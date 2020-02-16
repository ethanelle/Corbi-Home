import numpy as np
import time
import os
import signal
import face_recognition as fr
from pathlib import Path
import aws_file_upload as afu
import threading
import re

def handler(signum, frame):
    print('Exiting Face Detection Program...')
    exit(2)

signal.signal(signal.SIGINT, handler)
print('Starting Face Detection Program...')
env_var = 'OPENCV_VIDEOIO_PRIORITY_MSMF'
if env_var not in os.environ:
    print('setting OPENCV_VIDEOIO_PRIORITY_MSMF to be 0')
    os.environ[env_var]="0"
import cv2

class FaceDetection(object):
    __face_cascade = cv2.CascadeClassifier(str(Path.cwd() / 'haarcascade_frontalface_default.xml'))
    __DEBUG = 0
    __STREAM = 0
    demoMax = 5

    def findFace(self, cap):
        error = True
        local_val = 0
        local_expected = 0
        found = False
        while(True):
            # Capture frame-by-frame
            ret, rframe = cap.read()
            if not ret:
                print("Can't receive frame (stream end?). Exiting ...")
                break
            frame = cv2.resize(rframe, (0, 0), fx=0.25, fy=0.25)
            # Our operations on the frame come here
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
            faces = self.__face_cascade.detectMultiScale(gray, 1.1, 3)
            if len(faces) == 1 and not found:
                found = True
                error = False
            if found:
                if local_expected is 0 and self.__DEBUG:
                    print("One face found to verify...")
                if len(faces) is 1:
                    local_val = local_val + 1
                local_expected = local_expected + 1
                sample = 25
                if self.__DEBUG and local_expected == sample:
                    print("val: " + str(local_val) + "\nexpected: " + str(local_expected))
                if local_expected >= sample:
                    if self.withinConfidence(local_val, local_expected, .85):
                        print("Focus Success.")
                        return rframe
                    else:
                        print("Focus Fail.")
                        found = False
                        local_val = 0
                        local_expected = 0
                        print("Retrying...")
                        continue
            else:
                if self.__DEBUG and not error:
                    error = True
                    if (error):
                        print("Trying again...\n")
                found = False

            if self.__STREAM:
                for (x,y,w,h) in faces:
                    cv2.rectangle(gray, (x,y), (x+w,y+h), (255,0,0), 2)
                    roi_gray = gray[y:y+h, x:x+w]
                    roi_color = frame[y:y+h, x:x+w]

            #Display the resulting frame
            if self.__STREAM:
                cv2.imshow('frame', gray)
                if cv2.waitKey(1) & 0xFF == ord('q'):
                    break


    def __init__(self, detectedDir, whitelistDir):
        self.detectedPath = Path.cwd() / detectedDir
        self.whitelistPath = Path.cwd() / whitelistDir
        self.whitelist_encodings = []
        self.whitelistName = []


    def withinConfidence(self, val, expected, percentage=0.99):
        alpha = (1-percentage)
        return self.withinLimits(val, expected*(1-alpha), expected)

    def withinLimits(self, val, min, max):
        if (val >= min and val <= max):
            return True
        return False

    def saveAndSend(self, img, demoStr, demoCount, demoExt):
        demoId = demoStr + str(demoCount)
        demoFile = str(self.detectedPath / (demoId + demoExt))
        demoAWS = 'detections/' + demoId + demoExt
        localFile = 'local/'+demoId+demoExt
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        cv2.imwrite(localFile, gray)
        name = self.isWhitelisted(fr.load_image_file(localFile))
        if name == "":
            print("Intruder Alert!")
            cv2.imwrite(demoFile, img)
            t1 = threading.Thread(target=afu.upload_to_aws, args=(demoFile, 'iot-corbi', demoAWS, ))
            t1.start()
        else:
            print("Welcome Home, %s" % name)

        # (LOCK) read whitelistDir/*.jpg into [] (UNLOCK)
        # import face_recognition as fr: -^   fr.load_image_file check img against []
        # (THREAD) upload if alert to alert/*.jpg aws_file_upload

    def startDemo(self):
        #FORNOW: READ DIR
        whitelist_images = []
        files = []
        find = re.compile(r"^[^.]*")
        for filename in os.listdir(str(self.whitelistPath)):
                if filename.endswith(".jpeg"):
                    files.append(filename)
                    self.whitelistName.append(re.search(find, filename).group(0))
        for f in files:
            whitelist_images.append(fr.load_image_file(str(self.whitelistPath / f)))
        for i in whitelist_images:
            self.whitelist_encodings.append(fr.face_encodings(i)[0])
        # start thread to download from aws every 3s (LOCK) write to whitelistDir/*.jpg (UNLOCK)
        cap = cv2.VideoCapture(0)
        if not cap.isOpened():
            exit()
        cv2.waitKey(0)
        demoCount = 0
        demoStr = 'demo'
        demoExt = '.jpeg'
        while True:
            demoCount = demoCount + 1
            print('Begin detection...')
            img = self.findFace(cap)
            if self.__DEBUG:
                print('Attempt to write to file...')
            t1 = threading.Thread(target=self.saveAndSend, args=(img, demoStr, demoCount, demoExt, ))
            #self.saveAndSend(demoStr, demoCount, demoExt)
            t1.start()
            if cv2.waitKey(1) & 0xFF == ord('q'):
                break
            if self.demoMax == 0:
                continue
            if demoCount == self.demoMax:
                break

        # When everything done, release the capture
        cap.release()
        cv2.destroyAllWindows()

    def isWhitelisted(self, img):
        face_locations = []
        face_encodings = []
        face_locations = fr.face_locations(img)
        face_encodings = fr.face_encodings(img, face_locations)
        name = ""
        for face_encoding in face_encodings:
            matches = fr.compare_faces(self.whitelist_encodings, face_encoding)
            if True in matches:
                first_match_index = matches.index(True)
                name = self.whitelistName[first_match_index]
                break
        return name

def main():
    x = FaceDetection('detections', 'whitelist')
    x.startDemo() #Takes demoMax detections then exits

if __name__ == '__main__':
    main()

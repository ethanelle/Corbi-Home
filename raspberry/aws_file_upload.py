import boto3
import time
import datetime
import picamera import PiCamera

from botocore.exceptions import NoCredentialsError

ACCESS_KEY = ""
SECRET_KEY = ""

def upload_to_aws(local_file, bucket, s3_file):
    s3 = boto3.client('s3', aws_access_key_id=ACCESS_KEY,
                      aws_secret_access_key=SECRET_KEY)

    try:
        s3.upload_file(local_file, bucket, s3_file)
        print("Upload Successful")
        return True
    except FileNotFoundError:
        print("The file was not found")
        return False
    except NoCredentialsError:
        print("Credentials not available")
        return False

def main():
    camera = PiCamera()
    camera.resolution = (1024, 768)
    camera.start_preview()
    time.sleep(2) # warmup time
    while(True):
        ts = time.time()
        st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
        camera.capture(str(st))
        time.sleep(2)
        upload_to_aws(st, 'iot-corbi', st)
        time.sleep(2)

if __name__ == '__main__':
    main()

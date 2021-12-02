import cv2
import socket
import tqdm
import os 

SEPARATOR = "<SEPARATOR>"
BUFFER_SIZE = 4096 # send 4096 bytes each time step

# the ip address or hostname of the server, the receiver
host = "localhost"
# the port, let's use 5001
port = 5001
# the name of file we want to send, make sure it exists
filename = "thrash.jpg"
# get the file size
filesize = os.path.getsize(filename)
# create the client socket
s = socket.socket()
print(f"[+] Connecting to {host}:{port}")
s.connect((host, port))
print("[+] Connected.")
# send the filename and filesize
s.send(f"{filename}{SEPARATOR}{filesize}".encode())
# start sending the file
progress = tqdm.tqdm(range(filesize), f"Sending {filename}", unit="B", unit_scale=True, unit_divisor=1024)
with open(filename, "rb") as f:
    while True:
        # read the bytes from the file
        bytes_read = f.read(BUFFER_SIZE)
        if not bytes_read:
            # file transmitting is done
            break
        # we use sendall to assure transimission in 
        # busy networks
        s.sendall(bytes_read)
        # update the progress bar
        progress.update(len(bytes_read))
# close the socket
s.close()



### Computer Vision 

def blur_person(image, factor=3.0):
    (h, w) = image.shape[:2]
    kW = int(w / factor)
    kH = int(h / factor)
    # ensure the width of the kernel is odd
    if kW % 2 == 0:
        kW -= 1
    # ensure the height of the kernel is odd
    if kH % 2 == 0:
        kH -= 1
    # apply a Gaussian blur to the input image using our computed
    # kernel size
    return cv2.GaussianBlur(image, (kW, kH), 0)


thres = 0.60  # Threshold to detect object

# video_path = r"C:\Users\Emmanuel\Videos\tuto lotus p2\YouCut_20200521_181116375.mp4"
# cap = cv2.VideoCapture(video_path)
cap = cv2.VideoCapture(0)

classFile = 'coco.names'
with open(classFile, 'rt') as f:
    classNames = f.read().rstrip('\n').split('\n')

configPath = 'ssd_mobilenet_v3_large_coco_2020_01_14.pbtxt'
weightsPath = 'frozen_inference_graph.pb'

net = cv2.dnn_DetectionModel(weightsPath, configPath)
net.setInputSize(320, 320)
net.setInputScale(1.0 / 127.5)
net.setInputMean((127.5, 127.5, 127.5))
net.setInputSwapRB(True)

while True:
    success, img = cap.read()
    classIds, confs, bbox = net.detect(img, confThreshold=thres)
    # print(classIds, bbox)

    if len(classIds) != 0:
        for classId, confidence, box in zip(classIds.flatten(), confs.flatten(), bbox):
            try:
                x, y, w, h = box
                cv2.rectangle(img, box, color=(0, 255, 0), thickness=2)
                cv2.putText(img, classNames[classId - 1].upper(), (box[0], box[1]),
                            cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)

                cv2.putText(img, str(round(confidence * 100, 2)), (box[0], box[1]),
                            cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)

                # If a person is detected, blur her
                if classId == 1:
                    person_img = img[y:y+h, x:x+w]
                    blured_person = blur_person(person_img)
                    img[y:y+h, x:x+w] = blured_person
            except:
                print("error")

    cv2.imshow('Output', img)
    cv2.waitKey(1)

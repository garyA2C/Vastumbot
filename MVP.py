import cv2
import time
from collections import Counter

from goprocam import GoProCamera
from goprocam import constants
# gopro = GoProCamera.GoPro()
# gopro.stream("udp://127.0.0.1:10000")

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
gpCam = GoProCamera.GoPro()
cap = cv2.VideoCapture("udp://127.0.0.1:10000")
# cap = cv2.VideoCapture(0)

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

det_info = []
while True:
    success, img = cap.read()
    classIds, confs, bbox = net.detect(img, confThreshold=thres)
    # print(classIds, bbox)

    if len(classIds) != 0:
        for classId, confidence, box in zip(classIds.flatten(), confs.flatten(), bbox):
            # try:
                if len(det_info) < 10:
                    det_info.append((classNames[classId-1].upper(), round(confidence*100,2)))
                
                    # for idx, val in enumerate(det_info):
                    #     print(idx, val)

                x, y, w, h = box
                cv2.rectangle(img, box, color=(0, 255, 0), thickness=2)
                # cv2.putText(img, classNames[classId - 1].upper(), (box[0], box[1]),
                #             cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)

                # cv2.putText(img, str(round(confidence * 100, 2)), (box[0], box[1]),
                #             cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)
                cv2.putText(img, det_info[0][0], (box[0], box[1]),
                            cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)

                cv2.putText(img, str(det_info[0][1]), (box[0], box[1]),
                            cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)
                if len(det_info) >= 10:
                    det_info = []
                # If a person is detected, blur her
                if classId == 1:
                    person_img = img[y:y+h, x:x+w]
                    blured_person = blur_person(person_img)
                    img[y:y+h, x:x+w] = blured_person
            # except:
            #     print("error")

    cv2.imshow('Output', img)
    cv2.waitKey(1)

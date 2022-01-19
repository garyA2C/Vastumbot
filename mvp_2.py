import cv2
import time
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


thres = 0.30  # Threshold to detect object
#video_path = "/Users/quentinvittoz/Downloads/test_velo.MP4"
#cap = cv2.VideoCapture(video_path)
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
frame_width = int(cap.get(3))
frame_height = int(cap.get(4))
   
size = (frame_width, frame_height)


result = cv2.VideoWriter('filename.avi', 
                         cv2.VideoWriter_fourcc(*'MJPG'),
                         10, size)

t= time.time()
while True:
    try:
        success, img = cap.read()
        classIds, confs, bbox = net.detect(img, confThreshold=thres)
    # print(classIds, bbox)
    

        if len(classIds) != 0:
            for classId, confidence, box in zip(classIds.flatten(), confs.flatten(), bbox):
                try:
                    if classId == 1:
                            print("lol")
                            person_img = img[y:y+h, x:x+w]
                            blured_person = blur_person(person_img)
                            img[y:y+h, x:x+w] = blured_person

                    if classId in [1,28,47,53,72,78,44]:
                        x, y, w, h = box
                        cv2.rectangle(img, box, color=(0, 255, 0), thickness=2)
                        cv2.putText(img, classNames[classId - 1].upper(), (box[0], box[1]),
                                    cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)

                        """cv2.putText(img, str(round(confidence * 100, 2)), (box[0], box[1]),
                                    cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)"""

                        if len(det_info) >= 10:
                            det_info = []
                        # If a person is detected, blur her
                        if classId == 1:
                            print("lol")
                            person_img = img[y:y+h, x:x+w]
                            blured_person = blur_person(person_img)
                            img[y:y+h, x:x+w] = blured_person
                            
                        if classId == 44 :
                            print("bottle")
                        if classId == 78 : 
                            print("microwave")
                    else:
                        continue
                except:
                    print("error")
            
    
        cv2.imshow('Output', img)
        result.write(img)
        cv2.waitKey(1)
    except:
        print("error2")
cap.release()
result.release()
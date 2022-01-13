import cv2
import time
import json 
import csv
import os

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


thres = 0.35  # Threshold to detect object
video_path = r"C:/Users/Dell/Downloads/test_velo.MP4"
cap = cv2.VideoCapture(video_path)
#cap = cv2.VideoCapture(0)

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
save_path = 'output/'
if not os.path.exists(save_path):
    os.makedirs(save_path)
i = -1
output = []
frame=0
while True:
    try:
        success, img = cap.read()
        frame += 1
        if not success:
            break
        classIds, confs, bbox = net.detect(img, confThreshold=thres)
        if len(classIds) != 0:
            for classId, confidence, box in zip(classIds.flatten(), confs.flatten(), bbox):
                try:
                    if classId in [28,29,44,46,72,80,78]:
                        i += 1
                        x, y, w, h = box
                        output.append((frame,i,x,y,w,h,classNames[classId - 1]))
                        cv2.rectangle(img, box, color=(0, 255, 0), thickness=2)
                        cv2.putText(img, classNames[classId - 1].upper(), (box[0], box[1]),
                                    cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)
                                
                        cv2.imwrite(save_path + str(i) + '.jpg', img)
                        
                        data = {"type": classNames[classId-1]}
                        with open('output/'+str(i)+'.txt', 'w') as outfile:
                            json.dump(data, outfile)

                        """cv2.putText(img, str(round(confidence * 100, 2)), (box[0], box[1]),
                                    cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)"""

                        if len(det_info) >= 10:
                            det_info = []
                        # If a person is detected, blur her
                        if classId == 1:
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
        pass
        # print("error2")
cap.release()
result.release()

print(output)

with open('output.csv', "w") as the_file:
    csv_out=csv.writer(the_file)
    csv_out.writerow(['frame','object','x','y','w','h','type'])
    for row in output:
        csv_out.writerow(row)


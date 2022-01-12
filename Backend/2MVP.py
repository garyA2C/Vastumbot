import cv2
import time
import json 
import math 

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
video_path = r"D:\Applications\pils\Vastumbot\Backend\test.mp4"
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
i = 0
output = []
# frame=0
while True:
    try:
        success, img = cap.read()
        # print(success)
        if not success:
            break
        # frame+=1
        classIds, confs, bbox = net.detect(img, confThreshold=thres)
    # print(classIds, bbox)
    

        if len(classIds) != 0:
            for classId, confidence, box in zip(classIds.flatten(), confs.flatten(), bbox):
                try:
                    if classId in [29,44,46,72,80,78]:
                        i += 1
                        x, y, w, h = box
                        output.append((i,x,y,w,h))
                        cv2.rectangle(img, box, color=(0, 255, 0), thickness=2)
                        cv2.putText(img, classNames[classId - 1].upper(), (box[0], box[1]),
                                    cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)
                                
                        cv2.imwrite(save_path + str(i) + '.jpg', img)
                        
                        data = {"type": classNames[classId-1]}
                        with open('output/'+str(i)+'.json', 'w') as outfile:
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
                    # print("error")
                    pass
            # print(output)
    
        cv2.imshow('Output', img)
        result.write(img)
        cv2.waitKey(1)
    except:
        pass
        # print("error2")
cap.release()
result.release()
# print(output)
index = 0
img = [ [] for i in range(37)]
change = True
while output:
    print("Processing",output[0])
    img[index].append(output[0])
    del output[0]
    if not output:
        break
    # print(output[0])
  
    print(math.dist([output[0][1], output[0][2]], [img[index][-1][1], img[index][-1][2]]) )

    if  math.dist([output[0][1], output[0][2]], [img[index][-1][1], img[index][-1][2]]) < 25:
        # print(output[0])
        # print(output[0][1], output[0][2])
        # print(img[index][-1][1], img[index][-1][1])
        print("Linking ", str(output[0][0]), str(img[index][-1]))
        print("----")
        img[index].append(output[0])
        del output[0]
        if not output:
            break
    else :
        index += 1
        if not output:
            break


# print(img)
for i in range(10):
    print(img[i])
    

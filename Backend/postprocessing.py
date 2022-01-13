import os
import pandas as pd
import re
from datetime import datetime
from dateutil import parser
import calendar
import json
import shutil


detection = pd.read_csv('output.csv')
idx = []

# Create function to filter out the pictures which didn't have significant changes compared to the previous
for i in range(len(detection)):
    try:
        now = detection['x'][i] + detection['y'][i] + detection['h'][i] + detection['w'][i]
        after = detection['x'][i+1] + detection['y'][i+1] + detection['h'][i+1] + detection['w'][i+1]
        if abs(after-now) > 100:
            idx.append(detection['object'][i])
    except:
        pass

# Split array into arrays of consecutive indexes
def split_non_consequtive(data):
    data = iter(data)
    val = next(data)
    chunk = []
    try:
        while True:
            chunk.append(val)
            val = next(data)
            if val != chunk[-1] + 1:
                yield chunk
                chunk = []
    except StopIteration:
        if chunk:
            yield chunk

# Select image in th middle of each array containing consecutive indexes
final_idx = []
for i in split_non_consequtive(idx):
    middleIndex = round(sum(i)/len(i)-1)
    final_idx.append(middleIndex)

# Delete contents of filtered images
arr = os.listdir('output')
arr.sort(key=lambda f: int(re.sub('\D', '', f)))
path = '../Server/data/'
if not os.path.exists(path):
    os.makedirs(path)
new_number = 0
for i in arr:
    if i.endswith(".jpg"):
        delete = True
        for j in final_idx:
            if int(i[:-4]) == j:
                delete = False
                new_path = path + str(new_number)
                if not os.path.exists(new_path):
                    os.makedirs(new_path)
                    shutil.copy2('output/'+str(j)+'.jpg', new_path+'/image.jpg')
                    shutil.copy2('output/'+str(j)+'.txt', new_path+'/info.txt')
                new_number += 1
        if delete == True:
            detection['object'][int(i[:-4])] = -1
    if delete == True:
        # os.remove("output/" + i)
        pass

gps = pd.read_csv('test_velo_full.csv')
gps = gps.rename(columns=({'date': 'timestamp', 'GPS (Lat.) [deg]':'lat', 'GPS (Long.) [deg]': 'lon'}))
del gps['cts'], gps['precision'], gps['fix'], gps['altitude system'], gps['GPS (Alt.) [m]'], gps['GPS (2D speed) [m/s]'], gps['GPS (3D speed) [m/s]'] 
del detection['x'], detection['y'], detection['w'], detection['h'], detection['frame']

len_det = len(detection)
len_gps = len(gps)

detection['lat'] = float(0)
detection['lon'] = float(0)
detection['timestamp'] = int(0)
detection['path'] = 'data/'
detection['status'] = 'active'
detection['id_user'] = 1
detection.replace('umbrella', 'nonrecyclable', inplace=True)
detection.replace('bottle', 'glass', inplace=True)

for idx_det in final_idx:
    idx_gps = round(idx_det*len_gps/len_det)
    temp = parser.parse(gps['timestamp'][idx_gps])
    timestamp = calendar.timegm(temp.timetuple())
    detection['timestamp'][idx_det] = int(timestamp)
    detection['lon'][idx_det] = gps['lon'][idx_gps] 
    detection['lat'][idx_det] = gps['lat'][idx_gps]

detection = detection[detection.object != -1]
detection = detection.reset_index(drop=True)

detection = detection[['path', 'lat', 'lon', 'timestamp', 'type', 'status', 'id_user']]
for i in range(len(detection)):
    detection['path'][i] = str(detection['path'][i]) + str(i) + '/image.jpg'

for i in range(len(detection)):
    data = {}
    data['path'] = str(detection['path'][i])
    data['lat'] = float(detection['lat'][i])
    data['lon'] = float(detection['lon'][i])
    data['timestamp'] = int(detection['timestamp'][i])
    data['type'] = str(detection['type'][i])
    data['status'] = str(detection['status'][i])
    data['id_user'] = int(detection['id_user'][i])
    new_path = path + str(i)
    with open(new_path+'/info.txt', 'w') as file:
        json.dump(data, file)

big_data = {}
for i in range(len(detection)):
    big_data[i] = {}
    big_data[i]['path'] = str(detection['path'][i])
    big_data[i]['lat'] = float(detection['lat'][i])
    big_data[i]['lon'] = float(detection['lon'][i])
    big_data[i]['timestamp'] = int(detection['timestamp'][i])
    big_data[i]['type'] = str(detection['type'][i])
    big_data[i]['status'] = str(detection['status'][i])
    big_data[i]['id_user'] = int(detection['id_user'][i])
new_path = '../Server/'
with open(new_path+'data.json', 'w') as file:
    json.dump(big_data, file)

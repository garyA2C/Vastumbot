import os

test = [(1, 404, 3, 69, 50), (2, 394, 4, 143, 221), (3, 173, 1, 42, 28), (4, 164, 18, 37, 28), (5, 163, 21, 29, 24), (6, 79, 146, 53, 43), (7, 45, 192, 68, 46), (8, 11, 299, 63, 52), (9, 535, 0, 27, 29), (10, 536, 2, 23, 36), (11, 538, 4, 22, 40), (12, 541, 6, 24, 43), (13, 544, 11, 24, 41), (14, 547, 20, 22, 41), (15, 553, 24, 18, 41), (16, 553, 32, 23, 36), (17, 554, 29, 23, 40), (18, 555, 34, 22, 51), (19, 557, 42, 22, 46), (20, 566, 55, 26, 48), (21, 561, 57, 21, 46), (22, 570, 69, 24, 48), (23, 576, 70, 25, 50), (24, 585, 74, 21, 53), (25, 591, 85, 22, 53), (26, 594, 90, 30, 50), (27, 606, 106, 29, 53), (28, 72, 0, 44, 41), (29, 47, 47, 50, 40), (30, 30, 63, 54, 48), (31, 30, 69, 49, 48), (32, 16, 87, 54, 54), (33, 9, 93, 61, 57), (34, 7, 101, 59, 57), (35, 385, 3, 99, 63), (36, 547, 211, 68, 73), (37, 555, 231, 77, 80)]

idx = []
idx2 = []


# Create function to filter out the pictures which didn't have significant changes compared to the previous
for i in range(len(test)):
    try:
        before = test[i-1][1] + test[i-1][2] + test[i-1][3] + test[i-1][4]
        now = test[i][1] + test[i][2] + test[i][3] + test[i][4]
        after = test[i+1][1] + test[i+1][2] + test[i+1][3] + test[i+1][4]

        coord = (test[i][1] - test[i+1][2])**2 + (test[i][2] - test[i+1][2])**2
        area = (test[i][3] - test[i+1][3])**2 + (test[i][4] - test[i+1][4])**2
        if abs(after-now) > 100:
            # print(test[i][0], abs(before-now), abs(after-now))
            idx.append(test[i][0])
        if coord > 10000 and area > 1000:
            # print(test[i][0], coord, area)
            pass
    except(IndexError):
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
    middleIndex = round(sum(i)/len(i))
    final_idx.append(middleIndex)

print(final_idx)
# Delete contents of filtered images
arr = os.listdir('output')
for i in arr:
    if i.endswith(".jpg"):
        delete = True
        for j in final_idx:
            if int(i[:-4]) == j:
                delete = False
    if delete == True:
        os.remove("output/" + i)
        # print(i)
        # pass

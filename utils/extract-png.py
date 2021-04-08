import sys
import os

header = [b'\x89', b'\x50', b'\x4e', b'\x47']
end = [b'\x49', b'\x45', b'\x4e', b'\x44', b'\xae', b'\x42', b'\x60', b'\x82']

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("extract-png.py <file-to-extrct> <folder-to-save-to>")
        sys.exit(1)
        
    f = open(sys.argv[1], 'rb')
    folder = sys.argv[2]
    
    match = 0
    x = 147
    header_start = False
    byte = f.read(1)
    data = []
    while byte:
        if not header_start:
            if byte == header[match]:
                match += 1
                if match == len(header):
                    header_start = True

                    match = 0
            else:
                match = 0
        else:
            data.append(byte[0])
            if byte == end[match]:
                match += 1
                if match == len(end):
                    header_start = False
                    match = 0
                    x += 1

                    savefile = os.path.join(folder, str(x) + ".png")
                    s = open(savefile, 'wb')
                    
                    s.write(b''.join(header))
                    s.write(b''.join([b.to_bytes(1, byteorder='big') for b in data]))
                    s.write(b''.join(end))
                        
                    s.close()
                    data = []

                    print("Finished writing file #" + str(x))
            else:
                match = 0
        
        byte = f.read(1)
    
    f.close()

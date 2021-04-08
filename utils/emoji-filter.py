import sys

# Filter a list of emoji codes and find out which ones
# are duplicates or need to be implemented
#
# Overwrites original emoji list text file
if __name__ == "__main__":
	if len(sys.argv) == 2:
		listFile = sys.argv[1]
	else:
		print("emoji-filter.py <emoji list>")
		sys.exit()
		
	f = open(listFile, 'r', encoding="utf8")
	lines = f.readlines()
	f.close()
	
	d = open('EmojiMap.java', 'r', encoding="utf8")
	content = d.read()
	d.close()
	
	emoji = []
	for line in lines:
		l = line.rstrip()
		if l:
			s = '"' + l + '"'
			if s not in content:
				print("Added: [" + l + "]")
				emoji.append(line)
			else:
				print("duplicate found: [" + l + "]")
	
	f = open(listFile, 'w', encoding="utf8")
	f.write("".join(emoji))
	f.close()

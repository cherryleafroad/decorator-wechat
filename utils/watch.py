import time, sys, os
from watchdog.observers import Observer  
from watchdog.events import PatternMatchingEventHandler

num = 1

class Handler(PatternMatchingEventHandler):
	patterns = ["*"]
	
	def process(self, event):
		global num
		if event.event_type == "created":
			p = list(os.path.split(event.src_path))
			# change last element
			p[len(p)-1] = str(num) + ".png"
			os.rename(event.src_path, os.path.join(*p))
			num += 1
			
	def on_created(self, event):
		self.process(event)

if __name__ == "__main__":
    args = sys.argv[1:]
    observer = Observer()
    observer.schedule(Handler(), path=args[0] if args else '.')
    observer.start()
    
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()

    observer.join()

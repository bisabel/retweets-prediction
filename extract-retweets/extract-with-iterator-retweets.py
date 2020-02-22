#!/usr/bin/python
# -*- coding: iso-8859-15 -*-
from TwitterAPI import *
from credential import *
from datetime import datetime
import time

#the file credential.py has the variables with the consumer and access
api = TwitterAPI(consumer_key, consumer_secret, access_token, access_token_secret)
since_id = 0
count = 1
id = 0
f=open("tweet.json", "a+")
while count > 0:
	r = TwitterPager(api, 'search/tweets', {'q':'On Friday I’ll join the climate strike in Hamburg! 14.00h at Heiligengeistfeld. See you there! #ClimateStrike #FridaysForFuture #schoolstrike4climate'
											,'max_id': since_id})
	for item in r.get_iterator(wait=15, new_tweets=False):
	    if 'text' in item:
			created_at = ''
			count+=1
			id = item['id']
			created_at = item['created_at']
			second = datetime.strptime(created_at, '%a %b %d %H:%M:%S +0000 %Y')
			ts = datetime.strftime(second,'%Y-%m-%d %H:%M:%S')
			print("{\"date\" : %s, \"seconds\" : %i, \"id\": %i } count %i " % (ts, time.mktime(second.timetuple()), id,count))
			f.write("{\"date\" : %s, \"seconds\" : %i, \"id\": %i , count %i }\r\n " % (ts, time.mktime(second.timetuple()), id, count))
	    elif 'message' in item and item['code'] == 88:
	        print 'SUSPEND, RATE LIMIT EXCEEDED: %s\n' % item['message']
	        break

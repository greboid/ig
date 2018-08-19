import os
import hashlib
import json
import pathlib
import sqlite3
import tempfile
from enum import Enum, auto
from io import BytesIO
from typing import List, Dict
import datetime
import requests
import yaml
from PIL import Image
from bs4 import BeautifulSoup
from requests import Session
import logging

logging.basicConfig(level=logging.DEBUG)
logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class MediaType(Enum):
    IMAGE = auto()
    SIDECAR = auto()
    VIDEO = auto()


class MediaObject:
    username: str
    shortcode: str
    type: str
    caption: str
    thumbnail: str
    media: str
    timestamp: int

    def __init__(self, username: str, shortcode: str, type: MediaType, caption: str, thumbnail: str, url: str, timestamp: int):
        self.username = username
        self.shortcode = shortcode
        self.type = type
        self.caption = caption
        self.thumbnail = thumbnail
        self.media = url
        self.timestamp = timestamp

    def __repr__(self):
        return "<MediaObject shortcode:%s type:%s>" % (self.shortcode, self.type)

    def __str__(self):
        return "type:%s - shortcode:%s" % (self.type, self.shortcode)


class Timeline:
    username: str
    end_cursor: str
    medias: List[MediaObject]
    count = 0
    session: Session
    rhx_gis: str
    data: Dict
    userid: str

    def __init__(self, username: str, count: int):
        self.username = username
        self.count = count
        self.medias = []
        self.session = Session()
        self.rhx_gis: str = None
        self.userid: str = None
        self.data = None

    def get_timelime(self):
        currentcount = 0
        self.data = self.get_shareddata(self.username)
        while currentcount < self.count:
            if self.data is not None:
                logger.debug("Data exists")
                try:
                    if 'entry_data' in self.data:
                        logger.debug("Using: entry_data")
                        edges = \
                            self.data['entry_data']['ProfilePage'][0]['graphql']['user']['edge_owner_to_timeline_media'][
                                'edges']
                    elif 'data' in self.data:
                        logger.debug("Using: data")
                        edges = self.data['data']['user']['edge_owner_to_timeline_media']['edges']
                    else:
                        logger.debug("Using: node")
                        edges = self.data['node']['edge_media_to_caption']['edges']
                except KeyError as e:
                    logger.error('Key Error', exc_info=True)
                    return
                logger.debug("Looping edges: " + str(len(edges)))
                if len(edges) == 0:
                    logger.debug("No edges, exiting.")
                    return
                for edge in edges:
                    node = edge['node']
                    if node['__typename'] == 'GraphImage':
                        logger.debug("Found image: " + node['shortcode'])
                        try:
                            self.addmedia(self.get_image(self.username, node['shortcode']))
                        except:
                            logger.debug("Unable to get post data")
                    elif node['__typename'] == 'GraphSidecar':
                        logger.debug("Found sidecar: " + node['shortcode'])
                        try:
                            self.addmedia(self.get_sidecars(self.username, node['shortcode']))
                        except:
                            logger.debug("Unable to get post data")
                    elif node['__typename'] == 'GraphVideo':
                        logger.debug("Found video: " + node['shortcode'])
                        try:
                            self.addmedia(self.get_video(self.username, node['shortcode']))
                        except:
                            logger.error("Unable to get post data", exc_info=True)
                    else:
                        raise TypeError('Unknown edge type.')
                currentcount = len(self.medias)
                if currentcount < self.count:
                    if 'entry_data' in self.data:
                        self.data = self.get_more(self.data['entry_data']['ProfilePage'][0]['graphql']['user']['edge_owner_to_timeline_media']['page_info']['end_cursor'])
                    else:
                        self.data = self.get_more(self.data['data']['user']['edge_owner_to_timeline_media']['page_info']['end_cursor'])
            else:
                raise EnvironmentError('Unable to get data.')

    def addmedia(self, media):
        self.medias.append(media)

    def get_video(self, username: str, shortcode: str) -> List[MediaObject]:
        data = self.get_shareddata(shortcode, 'p')
        caption = ''
        if len(data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['edge_media_to_caption'][
                'edges']) > 0:
            caption = data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['edge_media_to_caption'][
                'edges'][0]['node']['text']
        createthumbnail(shortcode, data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['display_url'])
        return [MediaObject(username, shortcode,
                            MediaType.VIDEO,
                            caption,
                            "/thumbs/" + shortcode + ".jpg",
                            data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['video_url'],
                            data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['taken_at_timestamp'])]

    def get_sidecars(self, username: str, shortcode: str) -> List[MediaObject]:
        data = self.get_shareddata(shortcode, 'p')
        caption = ''
        if len(data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['edge_media_to_caption'][
                                'edges']) > 0:
            caption = data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['edge_media_to_caption'][
                                'edges'][0]['node']['text']
            logger.debug("Number of entries: " + str(len(data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['edge_sidecar_to_children']['edges'])))
        medias = []
        for index, edge in enumerate(data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['edge_sidecar_to_children']['edges']):
            logger.debug("Entry #"+str(index))
            createthumbnail(shortcode + str(index), edge['node']['display_url'])
            medias.append(MediaObject(username, shortcode + str(index),
                        MediaType.IMAGE,
                        caption,
                        "/thumbs/" + shortcode + str(index) + ".jpg",
                        edge['node']['display_url'],
                        data['entry_data']['PostPage'][0]['graphql']['shortcode_media'][
                            'taken_at_timestamp']))
        return medias

    def get_image(self, username: str, shortcode: str) -> List[MediaObject]:
        data = self.get_shareddata(shortcode, 'p')
        caption = ''
        if len(data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['edge_media_to_caption'][
                                'edges']) > 0:
            caption = data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['edge_media_to_caption'][
                                'edges'][0]['node']['text']
        createthumbnail(shortcode, data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['display_url'])
        return [MediaObject(username, shortcode,
                                           MediaType.IMAGE,
                                           caption,
                                           "/thumbs/" + shortcode + ".jpg",
                                           data['entry_data']['PostPage'][0]['graphql']['shortcode_media'][
                                               'display_url'],
                                           data['entry_data']['PostPage'][0]['graphql']['shortcode_media'][
                                               'taken_at_timestamp'])]

    def get_url(self, id: str, path: None):
        if path is None:
            return 'https://www.instagram.com/{0}'.format(id)
        else:
            return 'https://www.instagram.com/{0}/{1}'.format(path, id)

    def get_shareddata(self, id: str, path=None):
        data = None
        response = (self.session.get(self.get_url(id, path)))
        html = BeautifulSoup(response.content, 'html.parser')
        for script in html.select('script'):
            if script.text.startswith('window._sharedData'):
                data = json.loads(script.text.split('window._sharedData = ')[1][:-1])
        if data is None:
            raise ValueError('Unable to find shared data.')
        self.rhx_gis = data['rhx_gis']
        if path is None:
            self.userid = data['entry_data']['ProfilePage'][0]['graphql']['user']['id']
        return data

    def get_more(self, after: str):
        variables = json.dumps({'id': self.userid, 'first': 12, 'after': after})
        params = [('query_id', '17888483320059182'), ('variables', variables)]
        response = self.session.get('https://www.instagram.com/graphql/query/', params=params,
                                    headers={'x-instagram-gis': self.get_ig_gis(variables)})
        return json.loads(response.content)

    def get_ig_gis(self, params: str):
        return hashlib.md5((self.rhx_gis + ":" + params).encode('utf-8')).hexdigest()

    def __repr__(self):
        return "<Timeline username:%s max:%s>" % (self.username, self.count)

    def __str__(self):
        return "username:%s - max count:%s" % (self.username, self.count)


class DBA:
    db: str
    conn: sqlite3.Connection

    def __init__(self, db: str):
        self.conn = None
        self.db = db

    def init(self):
        self.conn = sqlite3.connect(self.db)
        self.inittables()

    def inittables(self):
        c = self.conn.cursor()
        c.execute('''CREATE TABLE IF NOT EXISTS profiles (
                                    id INTEGER PRIMARY KEY,
                                    name TEXT UNIQUE
                                    )''')
        c.execute('''CREATE TABLE IF NOT EXISTS profile_users (
                                    id INTEGER PRIMARY KEY,
                                    userID INT,
                                    profileID INT,
                                    UNIQUE(userID, profileID)
                                    )''')
        c.execute('''CREATE TABLE IF NOT EXISTS users (
                                id INTEGER PRIMARY KEY,
                                username TEXT UNIQUE,
                                lastpoll INTEGER
                                )''')
        c.execute('''CREATE TABLE IF NOT EXISTS medias (
                                shortcode TEXT PRIMARY KEY,
                                username TEXT,
                                thumbnailURL TEXT,
                                imageURL TEXT,
                                caption TEXT,
                                timestamp INTEGER
                            )''')
        self.conn.commit()

    def close(self):
        self.conn.close()

    def addprofiles(self, profiles: dict):
        if profiles is not None:
            for profile in profiles:
                for key, users in profile.items():
                    self.conn.cursor().execute('''insert or ignore into profiles(name) values (?)''', (key,))
                    for user in users:
                        self.conn.cursor().execute('''insert or ignore into users(username) values (?)''', (user,))
                        profileid = self.conn.cursor().execute('''select id from profiles where name=?''', (key,)).fetchone()[0]
                        userid = self.conn.cursor().execute('''select id from users where username=?''', (user,)).fetchone()[0]
                        self.conn.cursor().execute('''insert or ignore into profile_users (profileid, userid) values (?, ?)''', (profileid, userid,))
            self.conn.commit()

    def pruneprofiles(self, profiles: dict):
        if profiles is None:
            self.conn.cursor().execute('''delete from profiles''')
            self.conn.cursor().execute('''delete from profile_users''')
            self.conn.cursor().execute('''delete from users''')
            self.conn.cursor().execute('''delete from medias''')
            self.conn.commit()
        else:
            allusers = []
            allprofiles = []
            for profile in profiles:
                for key, users in profile.items():
                    allprofiles.append(key)
                    for user in users:
                        allusers.append(user)
            pruneusers = set(x[0] for x in self.conn.cursor().execute('''select username from users''').fetchall()) - set(allusers)
            pruneprofiles = set(x[0] for x in self.conn.cursor().execute('''select name from profiles''').fetchall()) - set(allprofiles)
            if len(pruneprofiles) > 0:
                for prune in pruneprofiles:
                    self.conn.cursor().execute('''delete from profiles where name=?''', (prune,))
                self.conn.cursor().execute('''delete from profile_users where profileid not in (select profileid from profiles)''')
            if len(pruneusers) > 0:
                for prune in pruneusers:
                    self.conn.cursor().execute('''delete from users where username=?''', (prune,))
                self.conn.cursor().execute('''delete from medias where username not in (select username from users)''')
                self.conn.commit()

    def addmedia(self, medias: MediaObject):
        for media in medias:
            self.conn.cursor().execute(
                '''insert or replace into medias(shortcode,username,thumbnailURL,imageURL,caption,timestamp) values (?,?,?,?,?,?)''',
                (media.shortcode, media.username, media.thumbnail, media.media, media.caption, media.timestamp))
            self.conn.commit()

    def getusers(self) -> List:
        users = []
        cursor = self.conn.cursor()
        cursor.execute('''select username from users''')
        for user in cursor.fetchall():
            users.append(user[0])
        return users

def createthumbnail(shortcode, url):
    pathlib.Path('static/thumbs').mkdir(parents=True, exist_ok=True)
    try:
        with tempfile.TemporaryFile() as fp:
            try:
                response = requests.get(url)
            except IOError as e:
                logger.error("IOError getting URL", exc_info=True)
                return
            if response.status_code != requests.codes.ok:
                logger.error("URL not found", exc_info=True)
            else:
                fp = Image.open(BytesIO(response.content))
                fp.thumbnail((200,200))
                fp.save("static/thumbs/" + shortcode + ".jpg", "JPEG")
    except IOError as e:
        logger.error("IOError creating thumbnail", exc_info=True)


def getconfig() -> Dict:
    try:
        with open('config.yml', 'r') as ymlfile:
            config = yaml.load(ymlfile)
            return config
    except FileNotFoundError:
        logger.error("Unable to find config file.")
        raise SystemExit


cfg = getconfig()
db = DBA(cfg['db'])
db.init()
db.addprofiles(cfg['profiles'])
db.pruneprofiles(cfg['profiles'])

timelines = []
for user in db.getusers():
    timelines.append(Timeline(user, 12))

for timeline in timelines:
    logger.debug("Getting timeline for: " + timeline.username)
    timeline.get_timelime()
    for media in timeline.medias:
        db.addmedia(media)

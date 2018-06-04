import hashlib
import json
import pprint
from enum import Enum, auto
from typing import List, Dict

import yaml
from bs4 import BeautifulSoup
from requests import Session


class MediaType(Enum):
    IMAGE = auto()
    SIDECAR = auto()
    VIDEO = auto()


class MediaObject:
    shortcode: str
    type: str
    caption: str
    thumbnail: str
    medias: List[str]

    def __init__(self, shortcode: str, type: MediaType, caption: str, thumbnail: str):
        self.shortcode = shortcode
        self.type = type
        self.caption = caption
        self.thumbnail = thumbnail
        self.medias = []

    def addmedia(self, media):
        self.medias.append(media)

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
                if 'entry_data' in self.data:
                    edges = self.data['entry_data']['ProfilePage'][0]['graphql']['user']['edge_owner_to_timeline_media']['edges']
                else:
                    edges = self.data['data']['user']['edge_owner_to_timeline_media']['edges']
                for edge in edges:
                    node = edge['node']
                    if node['__typename'] == 'GraphImage':
                        self.addmedia(self.get_image(node['shortcode']))
                    elif node['__typename'] == 'GraphSidecar':
                        self.addmedia(self.get_sidecars(node['shortcode']))
                    elif node['__typename'] == 'GraphVideo':
                        self.addmedia(self.get_video(node['shortcode']))
                    else:
                        raise TypeError('Unknown edge type.')
                currentcount = len(self.medias)
                if currentcount < self.count:
                    self.data = self.get_more(
                        self.data['entry_data']['ProfilePage'][0]['graphql']['user']['edge_owner_to_timeline_media'][
                            'page_info']['end_cursor'])
            else:
                raise EnvironmentError('Unable to get data.')

    def addmedia(self, media):
        self.medias.append(media)

    def get_video(self, shortcode: str) -> MediaObject:
        data = self.get_shareddata(shortcode, 'p')
        media = MediaObject(shortcode,
                            MediaType.VIDEO,
                            data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['edge_media_to_caption'][
                                'edges'][0]['node']['text'],
                            data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['display_url'])
        media.addmedia(data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['video_url'])
        return media

    def get_sidecars(self, shortcode: str) -> MediaObject:
        data = self.get_shareddata(shortcode, 'p')
        media = MediaObject(shortcode,
                            MediaType.SIDECAR,
                            data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['edge_media_to_caption'][
                                'edges'][0]['node']['text'],
                            data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['display_url'])
        for edge in data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['edge_sidecar_to_children'][
            'edges']:
            media.addmedia(edge['node']['display_url'])
        return media

    def get_image(self, shortcode: str) -> MediaObject:
        data = self.get_shareddata(shortcode, 'p')
        media = MediaObject(shortcode,
                            MediaType.IMAGE,
                            data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['edge_media_to_caption'][
                                'edges'][0]['node']['text'],
                            data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['display_url'])
        media.addmedia(data['entry_data']['PostPage'][0]['graphql']['shortcode_media']['display_url'])
        return media

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
                data = json.loads(script.text.split(' = ')[1][:-1])
        if data is None:
            raise ValueError('Unable to find shared data.')
        self.rhx_gis = data['rhx_gis']
        if path is None:
            self.userid = data['entry_data']['ProfilePage'][0]['graphql']['user']['id']
        return data

    def get_more(self, after: str):
        variables = '{{"id":"{0}","first":"{1}","after":"{2}"}}'.format(self.userid, 12, after)
        params = [('query_id', '17888483320059182'), ('variables', variables)]
        response = self.session.get('https://www.instagram.com/graphql/query/', params=params, headers={'x-instagram-gis': self.get_ig_gis(
                self.rhx_gis,
            variables
            )})
        return json.loads(response.content)

    def get_ig_gis(self, rhx_gis: str, params: str):
        data = rhx_gis + ":" + params
        return hashlib.md5(data.encode('utf-8')).hexdigest()

    def __repr__(self):
        return "<Timeline username:%s max:%s>" % (self.username, self.count)

    def __str__(self):
        return "username:%s - max count:%s" % (self.username, self.count)


try:
    with open("config.yml", 'r') as ymlfile:
        cfg = yaml.load(ymlfile)
except FileNotFoundError:
    print('Unable to find config file.')
    raise SystemExit

timelines = []
for user in cfg['users']:
    timelines.append(Timeline(user, 13))

print('Starting')
for timeline in timelines:
    timeline.get_timelime()
    print(timeline.medias)
print('Finished')

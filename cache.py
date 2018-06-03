import hashlib
import json
from enum import Enum, auto
from typing import List, Dict

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
        print('Entering loop.')
        while currentcount < self.count:
            print('Current count: ' + str(currentcount))
            print('Data: ', end='\t')
            print(self.data)
            if self.data is not None:
                edges = self.data['entry_data']['ProfilePage'][0]['graphql']['user']['edge_owner_to_timeline_media'][
                    'edges']
                for edge in edges:
                    node = edge['node']
                    if node['__typename'] == 'GraphImage':
                        print('Adding an image')
                        self.addmedia(self.get_image(node['shortcode']))
                    elif node['__typename'] == 'GraphSidecar':
                        print('Adding a sidecar')
                        self.addmedia(self.get_sidecars(node['shortcode']))
                    elif node['__typename'] == 'GraphVideo':
                        print('Adding a video')
                        self.addmedia(self.get_video(node['shortcode']))
                    else:
                        raise TypeError('Unknown edge type.')
                currentcount = len(self.medias)
                self.data = self.get_more(self.data['entry_data']['ProfilePage'][0]['graphql']['user']['edge_owner_to_timeline_media']['page_info']['end_cursor'])
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

    def get_url(self, id: str, path:None):
        if path is None:
            return 'https://www.instagram.com/{0}'.format(id)
        else:
            return 'https://www.instagram.com/{0}/{1}'.format(path, id)

    def get_shareddata(self, id: str, path=None):
        data = None
        html = BeautifulSoup((self.session.get(self.get_url(id, path))).content, 'html.parser')
        for script in html.select('script'):
            if script.text.startswith('window._sharedData'):
                data = json.loads(script.text.split(' = ')[1][:-1])
        if data is None:
            raise ValueError('Unable to find shared data.')
        self.rhx_gis = data['rhx_gis']
        print(json.dumps(data, indent=2))
        self.userid = data['entry_data']['ProfilePage'][0]['graphql']['user']['id']
        return data

    def get_more(self, after: str):
        self.update_ig_gis_header({('id', self.userid), ('first', 12), ('after', after)})
        params = [('query_id', '17888483320059182'), ('id', self.userid), ('first', 12), ('after', after)]
        response = self.session.get('https://www.instagram.com/graphql/query/', params=params)
        return json.loads(response.content)

    def get_ig_gis(self, rhx_gis, params):
        data = rhx_gis + ":" + params
        return hashlib.md5(data.encode('utf-8')).hexdigest()

    def update_ig_gis_header(self, params):
        self.session.headers.update({
            'x-instagram-gis': self.get_ig_gis(
                self.rhx_gis,
                params
            )
        })


timeline = Timeline('hrvy', 24)
print('Starting')
print(timeline.get_timelime())
print('Finished')

# requests.get('http://youraddress.com', params=evt.fields)
# params=[('name1','value11'), ('name1','value12'), ('name2','value21')]
# https://www.instagram.com/graphql/query/?query_id=17888483320059182&variables={"id":"%s","first":12,"after":"%s"}

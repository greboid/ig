from flask import Flask
from flask import jsonify
from flask import request
from flask import g
from flask import send_from_directory

import sqlite3

DATABASE = './database.sqlite'


def get_db():
    db = getattr(g, '_database', None)
    if db is None:
        db = g._database = sqlite3.connect(DATABASE)
    return db


def inittables():
    db = sqlite3.connect(DATABASE)
    c = db.cursor()
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
    db.commit()


def dict_factory(cursor, row):
    d = {}
    for idx, col in enumerate(cursor.description):
        d[col[0]] = row[idx]
    return d

inittables()
app = Flask(__name__)

@app.route('/feed')
def feed():
    start = request.args.get('start', default=0, type=int)
    count = request.args.get('count', default=5, type=int)
    get_db().row_factory = dict_factory
    cursor = get_db().cursor()
    cursor.execute(
        '''SELECT shortcode, medias.username as source, thumbnailURL as thumb, imageURL as url, caption as caption, timestamp 
        FROM medias 
        LEFT JOIN users on users.username=medias.username 
        ORDER BY timestamp 
        DESC LIMIT ? 
        OFFSET ?''', (count, start))
    rows = cursor.fetchall()
    return jsonify(rows)


@app.route('/')
def index():
    return app.send_static_file('index.html')


@app.route('/js/<path:filename>')
def js(filename):
    print(filename)
    return send_from_directory('./static/js/', filename)

@app.route('/css/<path:filename>')
def css(filename):
    print(filename)
    return send_from_directory('./static/css/', filename)

@app.route('/css/<path:filename>')
def thumbs(filename):
    return send_from_directory('./static/thumbs/', filename)


if __name__ == '__main__':
    app.run(debug=True)

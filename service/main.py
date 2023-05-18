from flask import Flask
from flask_restful import Api, Resource
from datetime import datetime, timedelta
import json
import requests
import pyrebaselite

# Api key for tomtom
apiKey = "PxDnLGrYZY6BCzyuPbDjRtJFygXqUNat"

# config for firebase
firebaseConfig = {
    "apiKey": "AIzaSyCzXZAZbaY4ChVbmHZFSOw3k7UDc3j3Y9g",
    "authDomain": "drivesafe-384814.firebaseapp.com",
    "databaseURL": "https://drivesafe-384814-default-rtdb.europe-west1.firebasedatabase.app/",
    "projectId": "drivesafe-384814",
    "storageBucket": "drivesafe-384814.appspot.com",
    "messagingSenderId": "1038829971338",
    "appId": "1:1038829971338:web:391d410c24e770ac3e4a73",
    "measurementId": "G-9NGM001EKF"
}


def getDataFromDatabase(name: str):
    firebase = pyrebaselite.initialize_app(firebaseConfig)
    db = firebase.database()
    data = db.child(name).get()
    return data.val()


app = Flask(__name__)
api = Api(app)


class HelloWorld(Resource):
    def get(self, name: str):
        result = getDataFromDatabase(name)
        return result


api.add_resource(HelloWorld, '/helloworld')

if __name__ == '__main__':
    app.run(debug=True)

# %%

import face_recognition
import cv2
import os
import glob
import numpy as np


class FaceRecognition:
    def __init__(self):
        self.known_face_encodings = []
        self.known_face_names = []

        # Resize frame für bessere Performance
        # test 0.25 funktioniert gut
        self.frame_resizing = 0.25

    def load_encoding_images(self, images_path):

        # Lade Bilder
        images_path = glob.glob(os.path.join(images_path, "*.*"))

        print("{} encoding images found.".format(len(images_path)))

        # Store image encoding and names
        for img_path in images_path:
            img = cv2.imread(img_path)
            rgb_img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

            # Get the filename only from the initial file path.
            basename = os.path.basename(img_path)
            (filename, ext) = os.path.splitext(basename)
            # Get encoding
            img_encoding = face_recognition.face_encodings(rgb_img)[0]

            # Store file name and file encoding
            self.known_face_encodings.append(img_encoding)
            self.known_face_names.append(filename)
        print("Encoding images loaded")

    def detect_known_faces(self, frame):
        small_frame = cv2.resize(frame, (0, 0), fx=self.frame_resizing, fy=self.frame_resizing)
        # Findet alle Gesichter und Gesichtscodierungen im aktuellen Videobild.
        # Konvertiert das Bild von BGR-Farbe (die OpenCV verwendet) in RGB-Farbe (die face_recognition verwendet)
        rgb_small_frame = cv2.cvtColor(small_frame, cv2.COLOR_BGR2RGB)
        face_locations = face_recognition.face_locations(rgb_small_frame, number_of_times_to_upsample=2, model="hog")
        # number_of_times_to_upsample - higher number finds smaller faces - default = 1
        # mode = "hog" faster than "cnn" - default = hog
        face_encodings = face_recognition.face_encodings(rgb_small_frame, face_locations)

        face_names = []
        for face_encoding in face_encodings:
            # Prüfen, ob das Gesicht mit den bekannten Gesichtern übereinstimmt
            matches = face_recognition.compare_faces(self.known_face_encodings, face_encoding)
            name = "Unknown"

            # Übereinstimmung in known_face_encodings
            # bekannte Gesicht mit dem geringsten Abstand zum neuen Gesicht.
            face_distances = face_recognition.face_distance(self.known_face_encodings, face_encoding)
            best_match_index = np.argmin(face_distances)
            if matches[best_match_index]:
                name = self.known_face_names[best_match_index]
            face_names.append(name)

        # In Numpy-Array umwandeln, um die Koordinaten bei der Größenänderung des Rahmens schnell anzupassen
        face_locations = np.array(face_locations)
        face_locations = face_locations / self.frame_resizing
        return face_locations.astype(int), face_names

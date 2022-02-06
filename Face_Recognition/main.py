import cv2
from faceRecognition import FaceRecognition
import playsound
import time


def userFeedback_sound(counter, name):
    # Zugang gewährt oder nicht gewährt nach gewisser Anzahl geprüfter Frames
    if counter == 4:
        # play sound file
        playsound.playsound("zugriff_gewaehrt_mit_Niklas.mp3")
        time.sleep(3)
        counter = 0
    elif counter == -4:
        # play sound file
        playsound.playsound("zugriff_verweigert_3.mp3")
        time.sleep(3)
        counter = 0
    elif name == "NiklasKugler" or name == "JakobHaeringer" or name == "JakobHaeringer_2":
        counter = counter + 1
    elif name == "Unknown":
        counter = counter - 1
    else:
        counter = 0
    return counter


def main():
    # Codiere Gesichter aus Samples aus Ordner images
    sfr = FaceRecognition()
    sfr.load_encoding_images("images/")

    # Kamera laden
    # 0 - LaptopKamera
    # 1 - Externe Webcam
    cap = cv2.VideoCapture(0)

    counter = 1
    cv2.namedWindow("window", cv2.WND_PROP_FULLSCREEN)
    cv2.setWindowProperty("window", cv2.WND_PROP_FULLSCREEN, cv2.WINDOW_FULLSCREEN)

    while True:
            ret, frame = cap.read()

            # detektiere Gesichter mit detect_known_face
            face_locations, face_names = sfr.detect_known_faces(frame)

            for face_loc, name in zip(face_locations, face_names):
                y1, x2, y2, x1 = face_loc[0], face_loc[1], face_loc[2], face_loc[3]

                # Farbe in BGR
                if name == "NiklasKugler":
                    cv2.putText(frame, "Hallo Niklas!", (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 1, (50, 200, 50), 1)
                    cv2.rectangle(frame, (x1, y1), (x2, y2), (50, 200, 50), 4)

                # Farbe in BGR
                elif name == "JakobHaeringer" or name == "JakobHaeringer_2":
                    cv2.putText(frame, text="Hallo Jakob!", org=(x1 - 5, y1 - 40), fontFace=cv2.FONT_HERSHEY_TRIPLEX,
                                fontScale=1, color=(50, 180, 50), thickness=1, lineType=cv2.LINE_AA)
                    cv2.rectangle(frame, (x1 - 5, y1 - 30), (x2 + 5, y2 + 20), color=(50, 180, 50), thickness=2)

                else:
                    cv2.putText(frame, text="Unbekannt!", org=(x1 - 5, y1 - 40),
                                fontFace=cv2.FONT_HERSHEY_DUPLEX, fontScale=1, color=(50, 50, 200), thickness=1)
                    cv2.rectangle(frame, (x1 - 5, y1 - 30), (x2 + 5, y2 + 20), (50, 50, 200), 4)
                # Sound Acceess to the shuttle
                # Offene Todos erweitern das System
                counter = userFeedback_sound(counter, name)

            # Frame auf Bildschirmgröße anpassen
            # frame_passend = cv2.resize(frame, (1920, 1080), interpolation=cv2.INTER_AREA)
            cv2.imshow("window", frame)

            # Beende Programm mit q
            if cv2.waitKey(1) & 0xFF == ord('q'):
                break
    cap.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()

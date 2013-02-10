Petit sketch de test arduino pour les moteurs pas-à-pas.

Commande par liaison série (9600 8N1)

chaque commande est une lettre suivie d'un ou 2 paramètres numériques

Lettre majuscule : paramètre en nombre de révolutions
lettre minuscule : paramètre en nombre de steps (200 steps par révolution)
paramètre positif : sens horaire
paramètre négatif : sens anti-horaire

D nb            moteur droit
g nb            moteur gauche
t nb1 nb2       les 2 moteurs
s nb1           configure la vitesse des moteurs en RPM

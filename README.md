# OtakuCentral
OtakuCentral è un'applicazione per appassionati di anime (serie animate) e manga (fumetti) giapponesi che come scopo principale permette di tenere traccia di episodi/film visti e di volumi letti. 

Per semplicità e per permettermi di implementare le fondamenta dell'app, il salvataggio di tale informazione al momento avviene in memoria attraverso l'utilizzo di DataStore, ma sto prendendo in considerazione il fatto di appoggiarmi ad un DB in cloud tipo Firestore.

All'interno dell'app faccio utilizzo di una REST API per ottenere le informazioni che vengono poi mostrate a schermo

Componenti:
-pagina di benvenuto (associata all'icona di una casa) riportante suggerimenti sui "top" contenuti da visionare o da leggere
-WIP: pagina di ricerca (con suggerimenti basati sui generi a scelta) raggiungibile tramite search bar.
    Da implementare la possibilità di far scegliere allo user se ricercare invece dei titoli manga. Il contenuto della pagina verrà riempito con suggerimenti divisi per "genere" preferito (impostabile da pagina del profilo)
-pagina con i risultati della ricerca (al momento mostra solo anime)
-pagina di descrizione di un anime/film, riportante l'eventuale lista di episodi disponibili (se dichiaro di aver visto un episodio in automatico segna tutti i precedenti). È possibile raggiungere tale pagina a partire da quella di benvenuto o dalla pagina in cui restituisco i risultati di ricerca.

Ancora da implementare nella totalità:
-possibilità di aggiungere un elemento in una watchlist
-una pagina dedicata alla descrizione di un manga, sarà molto simile a quella di descrizione di un anime, dando qui però la possibilità di segnare i volumi letti
-pagina profilo (icona user) riportante una lista di anime/film completati e una di manga letti, con anche un menù di preferenze per indicare i generi che verranno utilizzati nella pagina di ricerca per mostrare i risultati
-pagina watch list (icona tv) che conterrà la lista di anime che si intende visionare o per cui ho salvato almeno un episodio, per ogni anime verrà riportato l'episodio successivo da guardare e si avrà la possibilità di segnarlo da lì tramite bottone
-pagina read list: come sopra ma legata alla lettura dei manga e riportando il successivo volume da leggere
-servizio periodico di notifica novità
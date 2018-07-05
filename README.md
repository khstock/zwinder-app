# zwinder-app
Building a dating app to improve my understanding of android development. Also as a project for University of Applied Sciences Kaiserslautern. [A demo Video of the current state of the app can be found here](https://www.youtube.com/watch?v=eBUVMyqWFP0&feature=youtu.be) (1:17min)
Note: most of the comments are currently in German, sorry about that

### Demo Gifs: account creation, swiping, profile picture upload 
![](https://media.giphy.com/media/lcqrxZKdL8Fs76J4FG/giphy.gif)
![](https://media.giphy.com/media/fCU1KaEbkz5f5eOfDi/giphy.gif)
![](https://media.giphy.com/media/1UPVdhDYK1Bx72AuDG/giphy.gif)

### The dating app currently features:
1. Creating and logging into your account
2. Setting up preferences and profile pictures
3. Swiping through user recommendations
4. Queries, Queries, lots of Queries to find the perfect user in every case

### To-Do:
1. Add actual functionality to gathered Match data
2. Implement chat
3. Implement app Notifications
4. More genders, more user preferences, e.g. Age

### How to run
For running the app you need to import this project to Android Studio and include your own **google-services.json** file from [Firebase's developer console](https://console.firebase.google.com/)

## Credit:
### Resources:
1. Inspiration and design: infamous dating app [Tinder](https://tinder.com/?lang=en),
2. also some ideas from [OkCupid](https://www.okcupid.com)
3. A very helpful youtube playlist to wrap your head around the basic necessities for such a project [Simcoders Youtube Videos](https://www.youtube.com/playlist?list=PLxabZQCAe5fio9dm1Vd0peIY6HLfo5MCf) on reacreating Tinder
4. Login and signup activity design: [Sourcey's material login layout](https://github.com/sourcey/materiallogindemo)
### Libraries
1. [Diolors Swipecards](https://github.com/Diolor/Swipecards/) for those awesome animations!
2. [Butterknife, View Binding Library](https://github.com/JakeWharton/butterknife)
3. [Glide, Image Loading Library](https://bumptech.github.io/glide/)
4. [Firebase Auth](https://firebase.google.com/docs/auth/) for easy authentication
5. [Firebase Database](https://firebase.google.com/docs/database/)
6. [Firebase Storage](https://firebase.google.com/docs/storage/)

### Other Resources
7. [Android Pattern Matches](https://developer.android.com/reference/android/util/Patterns), most of the Android developer documentation actually
8. [SQL vs. NoSQL, TheGeekStuff](https://www.thegeekstuff.com/2014/01/sql-vs-nosql-db/?utm_source=tuicool), helpful article to wrap your head around the difference of working with firebase NoSQL Database

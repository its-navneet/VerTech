# Android-Study-Jams

# VerTech

## Problem Statement
Initially every beginner finds it tough to start and work under a domain of their interest due to lack of like-minded mates and guides of the same avenue. Tech geeks find it difficult to connect with people having the same affinity. There is a lack of a proper environment where they can focus and carry out their ideas without getting distracted by interfering and unwanted ads .Android applications are a very convenient way to create an environment where different students(beginners as well as advanced), can share a common platform to create as well as share their project ideas and recent personal achievements. 


## Proposed Solution
Vertech is an ad free instant messaging and digital distribution platform where tech enthusiasts can jam with peers of their respective domains and collaborate on different projects. A league can share feeds of their amuse or new ideas of their respective domain. Many innovative ideas , projects can be cultivated on this user-friendly platform. The user can also maintain a to-do list of the projects to be done with his/her respective team mates. To avoid any delay in the flow of ideas, the user will have the liberty to have secure conversations through messaging with his peers.


## Functionality and Concepts used

The user interface of our application is made very subtle and easy to use.Following are few of our selection of Kotlin basic concepts to achieve a fairly elevated application :

**Constraint Layout:** For better performance of the app and to acquire a responsive view we chose this concept which was quite adaptable for different screen sizes. Most of the .xml files of the applications are made using constraint layout.

**Simple & Easy Views Design:** Use of familiar audience EditText with hints and interactive buttons made it easier for students to register or sign in without providing any detailed instructions pages. 

**Navigation Component:** To navigate between stations within our app which provides a smooth user experience irrespective of the component used.

**View Model:** The View Model functionality is used in the ‘Feed’ section of the application as well as in the Todo Projects section of the application. 

**LiveData:** The Live data functionality is used in the ‘Todo Projects’ section to update the items in the recycler view as well as delete them on click.

**Recycler View:** This functionality is used in the home page to view daily feed, the chat section as well as in the todo list for making projects.

**Room database:** The Room database functionality is used to implement the ‘Todo Projects’ section. The items of the recycler view(Project Name, Description and Projects Members) are locally stored in the phone’s File Manager in a folder named App DB.

**Internet Connectivity:** The application uses Firebase Authentication for Login/Sign up and Firebase Realtime Database for the chat functionality as well as for the uploading a post and retrieving all the post in feed of the user.

## Screenshots
Registration Page- 
<img src="https://github.com/its-navneet/VerTech/blob/master/register.jpg" width="250" height="500" />

Home page/Feed Section-
<img src="https://github.com/its-navneet/VerTech/blob/master/home.jpg" width="250" height="500" />

User Chats-
<img src="https://github.com/its-navneet/VerTech/blob/master/chats.jpg" width="250" height="500" />

Specific Chat-
<img src="https://github.com/its-navneet/VerTech/blob/master/Individualchats.jpg" width="250" height="500" />

Todo Projects-
<img src="https://github.com/its-navneet/VerTech/blob/master/projectstodolist.jpg" width="250" height="500" />


Create Post-
<img src="https://github.com/its-navneet/VerTech/blob/master/newpost.jpg" width="250" height="500" />

Peer Search-
<img src="https://github.com/its-navneet/VerTech/blob/master/search.jpg" width="250" height="500" />

User profile-
<img src="https://github.com/its-navneet/VerTech/blob/master/myprofile.jpg" width="250" height="500" />


## Demo

Demo Video

Registration Page- 
<img src="https://github.com/its-navneet/VerTech/blob/master/VerTech_Demo.gif" width="280" height="550" />


## Application link & future scope-
Currently our app is in the testing phase within our associates and technical clubs.You can access the app here: https://drive.google.com/file/d/128Pf0oA-7LETtYypuuJBKtUIILvQOylh/view?usp=sharing

Very soon our application will be launched on Google Play Store and will be functional in our university and and we hope it will soon reaches other universities too. We intend, by the end of the year, most schools and institutions will use our community app to explore, learn and create innovative ideas. We are planning to append more user-friendly experience and group conversations of dedicated domains.


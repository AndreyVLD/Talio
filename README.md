<img src="https://codecov.io/gh/AndreyVLD/Talio/branch/main/graphs/tree.svg?token=RG62SHCO3V" height="96">

[![codecov](https://codecov.io/gh/AndreyVLD/Talio/branch/main/graph/badge.svg?token=RG62SHCO3V)](https://codecov.io/gh/AndreyVLD/Talio)
## Description
Talio is a task management application that allows users to create, 
organize, and manage tasks efficiently. The application consists of a 
separate server and client application, with the server providing the backend 
functionality for data storage, retrieval and processing, while 
the client provides an intuitive and responsive UI.

Talio has an extensive amount of features on both the client-side and 
server-side, client-side features include: an overview of recently visited 
boards, drag-and-drop functionality for various UI elements, a tag system 
for organizing tasks, and a comprehensive set of keyboard shortcuts for 
faster navigation. The server supports both WebSocket and long-polling 
endpoints, allowing for real-time communication between the server and client.


## Group members

| Profile Picture                                                                                              | Name               | Email                                |
|--------------------------------------------------------------------------------------------------------------|--------------------|--------------------------------------|
| <img src="https://avatars.githubusercontent.com/u/11707259?v=4?s=80" height="96">                            | Jules Hummelink    | j.j.h.l.hummelink@student.tudelft.nl |
| ![](https://secure.gravatar.com/avatar/6f9cd7759c34f16bcc135eed0b75e939?s=96&d=identicon)                    | Ivar van Loon      | i.s.vanloon@student.tudelft.nl<br/>  |
| ![](https://secure.gravatar.com/avatar/49504bbc517408ca02e9182acb2ea231?s=96&d=identicon)                    | Andrei Vlad Nicula | a.v.nicula-1@student.tudelft.nl<br/> |
| <img src="https://gitlab.ewi.tudelft.nl/uploads/-/system/user/avatar/5378/avatar.png?s=96" height="96">      | Dobrin Bashev      | d.bashev-1@student.tudelft.nl        |
| <img src="https://gitlab.ewi.tudelft.nl/uploads/-/system/user/avatar/6145/avatar.png?width=96" height="96">  | Rares Burghelea    | R.Burghelea@student.tudelft.nl       |
| <img src="https://secure.gravatar.com/avatar/bcbda325caafa19bc0ab59f3850be455?s=80&d=identicon" height="96"> | Samuil Radulov     | S.T.Radulov-1@student.tudelft.nl     |

## How to run it

For the server:
1. Navigate to the project's root folder
2. Execute `./gradlew :server:bootRun` in a terminal

For the client:
1. Navigate to the project's root folder
2. Execute `./gradlew :client:run` in a terminal

## How to contribute to it

1. Clone the repository
2. Open the project in an IDE of your choice
3. Build/run the project using Gradle (we use JDK17) to verify everything works
   - `./gradlew :server:bootRun` and `./gradlew :client:run`
4. Configure checkstyle with the `checkstyle.xml` provided in the root of the repository
5. Create a feature branch with your changes
6. Create a Merge Request for said branch & wait for review

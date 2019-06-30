# Software Development for Networks and Telecommunications


This is a project is a network developed to avoid car collisions/accidents on a road, by informing the drivers for potential dangers from the cars around them. The network is consisted of two android devices (which are considered as cars), an Edge Server, a Backhaul Server and a Database.
![Network structure](https://raw.githubusercontent.com/sokb/Software-Development-for-Networks-and-Telecommunications/master/network_sructure.png?raw=true)
## Collaborators:
* [Symela Komini](https://github.com/Symela)
* [Vaggelis Tzironis](https://github.com/TorenIvan)
* [Sokrates Beis](https://github.com/sokb)
* [John Manolakis](https://github.com/Johnman97)
## Project Structure:
### Android Devices
The devices collect electroencephalographic (EEG) data from the sensors attached to the drivers, in order to detect if the drivers fall asleep during driving. They communicate with the Edge Server using the MQTT protocol.
### Edge Server
The Edge Server collects the EEG data sent by the Android terminals and is responsible for the classification of those data in order to determine their status. After the classification is done, it sends status messages back to the devices using the MQTT and also to the Backhaul Server using a socket.
### Backhaul Server
The Backhaul Server is responsible for the training of the model, which will be sent to the Edge Server and then be used for the classification. It also stores to the Database the status messages it receives from the Edge Server.
### MySQL Database
The Database is used to keep a status history of the Android devices

***For more information, please read the Readme and Readme2 pdf files included.***
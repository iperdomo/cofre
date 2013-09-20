# Cofre

It's a small application enabling an Android user to upload an image to
a *known* and *trusted* server.

A user takes a picture and wants to share it with his/her family,
friends, but doesn't want to share it by uploading it to the servers
of Facebook, Google, WhatsApp or any similar service.

Instead of that, he/she selects to share it with __Cofre__ and,

* a sampled version of the picture gets uploaded to a known server
* the server replies an `id` of the image
* the client builds a URL and __copy__ the resulting location to the clipboard
* the only remaining step is to __paste__ the link into the favorite IM service.


## Security

Cofre doesn't aim to encrypt and/or restrict the access to the image,
for that you can setup the server side part of Cofre with a service
like Nginx with SSL and basic authentication.

## Disclaimer

The code is considered in _alpha_ version (perhaps will remain like
this for a long time), but it solves the basic use case.


## License

The program is released under the Apache License 2.0


## Attribution

Cofre icon is based on `treasure_chest2_up` icon released under the 
*CC-BY*. You can grab the original version at 
[findicons.com](http://findicons.com/icon/494243/treasure_chest2_up)

 

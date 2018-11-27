# JSPNego

This library implements an SPNego protocol to use an HTTP proxy with Kerberos authentication via a 
keytab or a cache to authenticate access on the SIS Web Gateway. A keytab file contains one or more 
pairs of identifiers plus encrypted key, derived from the Kerberos password.

### Synopsis

This document provides the motivation of the project and the different instructions to both install
and use the DOI-Server. 

### Motivation

This library was designed to save development time for projects hosted at CNES and wishing to make 
requests to the web.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for 
development and testing purposes. See deployment for notes on how to deploy the project on a 
live system.

### Prerequisities

What things you need to install the software and how to install them

```
Openjdk version 1.8
Apache Maven 3.5.2
Git version 2.17.1
```

### Installing

Clone the repository

```
git clone https://github.com/CNES/JSPNego.git && cd JSPNego
```

Compile and run the tests

```
mvn install
```

End with an example of getting some data out of the system or using it for a little demo

## Example Use

* Create an environment variable KRB5CCNAME

```
export KRB5CCNAME=DIR:$HOME/.krb/
```

 * Create a keytab

```
ipa-getkeytab -p <username>@SIS.CNES.FR -k <username>.keytab -P
```


//Show what the library does as concisely as possible, developers should be able to figure out **how** your project solves their problem by looking at the code example. Make sure the API you are showing off is obvious, and that your code is short and concise.

## API Reference

Depending on the size of the project, if it is small and simple enough the reference docs can be added to the README. For medium size to larger projects it is important to at least provide a link to where the API reference docs live.


## Built With
* Maven


## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/Cnes/JSPNego/tags). 

## Authors

* **Sébastien Etcheverry** - *Initial work*
* **Jean-Christophe Malapert** - *Initial work* - [Jean-Christophe Malapert](https://github.com/J-Christophe)

See also the list of [contributors](https://github.com/Cnes/JSPNego/graphs/contributors) who participated in this project.

## License

This project is licensed under the **LGPLV3** - see the [LICENSE.md](https://github.com/Cnes/JSPNego/blob/master/COPYING.LESSER) file for details.



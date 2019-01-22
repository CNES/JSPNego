# JSPNego
This JAVA library is built to make HTTP requests through/without a proxy. Several proxies are 
supported :
<ul>
<li>Proxy without authentication</li>
<li>Proxy with basic authentication</li>
<li>Proxy with JSPNego with JAAS configuration file</li>
<li>Proxy with JSPNego using the API</li>
</ul> 
JSPNego is a Single Sign On (SSO). This SSO uses <i>The Simple and Protected GSS-API Negotiation 
Mechanism (IETF RFC 2478)</i> (<b>SPNEGO</b>) as authentication protocol.


### Synopsis

This document provides the motivation of the project and the different instructions to both install
and use a proxy with the following authentications: JSPNego, basic authentication. 

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

* Compile and run the tests

```
mvn install
```

* Run the integration tests

```
mvn verify -P integration-test
```

## Example Use

* Create an environment variable KRB5CCNAME

```
export KRB5CCNAME=DIR:$HOME/.krb/
```

 * Create a keytab

```
ipa-getkeytab -p <username>@<server> -k <keytabFileName> -P
```

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



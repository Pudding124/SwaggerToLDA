# API Swagger analyze

Dear All:
The purpose of this project is to analyze API swagger, Find the most relevant words based on the content, then use the words expand to more word.

## Swagger Content
There are different ways to deal with different content, we have two different things, Resource and Operations.
#### Resource
> $.info.title

> $.info.description

> $.info.x-tag

#### Operations
> $.paths.{path_name}

> $.paths.{path_name}.{action}

> $.paths.{path_name}.{action}.description

> $.paths.{path_name}.{action}.operationId

> $.paths.{path_name}.{action}.summary

## Latent Dirichlet Allocation (LDA)
latent Dirichlet allocation (LDA) is a generative statistical model that allows sets of observations to be explained by unobserved groups that explain why some parts of the data are similar.

[https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation](https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation)

## WordNet
WordNet is a lexical database for the English language.
[https://en.wikipedia.org/wiki/WordNet](https://en.wikipedia.org/wiki/WordNet)

you need to download the WordNet 2.1 or WordNet 3.0 in here
[https://wordnet.princeton.edu/download/current-version](https://wordnet.princeton.edu/download/current-version)
(notice: Don't forget, You must change the file path of the WordNet program)

```bash
#in WordNetExpansion row 53
private String wordNetPath = "/Users/xumingjen/WordNet-3.0/dict"; // "/home/mis101bird/WordNet/dict";

#in wordnet_config.xml row 42
<param name="dictionary_path" value="/Users/xumingjen/WordNet-3.0/dict"/>
```

## Example
You can use JSON or YMAL as your input data,then post it to http://localhost:8080/send/swagger
like this: [JSON](https://api.apis.guru/v2/specs/1forge.com/0.0.1/swagger.json),  [YAML](https://api.apis.guru/v2/specs/1forge.com/0.0.1/swagger.yaml)

Also, you can post only URL to http://localhost:8080/send/swaggerURL
(But URL is not support the YAML)

## Video
[https://www.youtube.com/watch?v=6u4xwOs2GUM](https://www.youtube.com/watch?v=6u4xwOs2GUM)

## License
[MIT](https://github.com/Pudding124/SwaggerToLDA/blob/master/LICENSE)

## API Online
There is a online api (but no WordNet, Only LDA parse word) --> [https://parse-swagger-topic.herokuapp.com/](https://parse-swagger-topic.herokuapp.com/)

```bash
It hava two mapping.

#You can put your swagger file (json or yaml), then it can work for you
https://parse-swagger-topic.herokuapp.com/send/swagger

#You can put your swagger url (format must be json), then it can work for you
https://parse-swagger-topic.herokuapp.com/send/swaggerURL
```

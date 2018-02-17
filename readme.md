Package com.sample.parser

## DataProviderJson is used for parsing a json message to an EnumMap.

* application.yml file contains the mappings of the json paths of each DataItemId for a given MessageType

* The Java class DataProviderJson is used for parsing the configuration of the json paths per DataItemId for the MessageType, when we enable the configuration for this class.
    **  Method Map<MessageType, MessagePaths> getMessagePaths():
        When we enable the configuration properties of this class (by using the annotation @EnableConfigurationProperties),
        then the EnumMap<MessageType, MessagePaths> messagePaths is populated by Spring. The configuration is set up in the application.yml file.

    **  Method Map<String, Map<String, String>> getDataItemDefaultJsonPaths(MessageType messageType):
        This method is used retrieving all the jsonPaths based of message version (Eg default, v1.1) for every message type.

    **  Method Map<DataItemId, Object> getDataItems(String sourceTopic, String payload) is used for parsing the json payload string and producing an EnumMap of DataItemId
        and its corresponding parsed value based on the DataItemId's json path.

Note: All the enumeration values used are only test values.


Package com.sample.aop

## AccessibleCountriesVerification class is using Aspect. This is used for checking user has access for the requested countries.

   *    Wherever in the code, the annotation Countries will be used, the method verifyAndPassAccessibleCountries will be executed and the user accessible countries
        will be verified before proceeding further. If user requests countries A,B,C and user has to only A and C, then a string collection containing of only A and C will
        be returned.






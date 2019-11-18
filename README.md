
# Anypoint Template: Workday to Salesforce Worker Migration

<!-- Header (start) -->
Moves a large set of workers from Workday to Salesforce. Trigger with an HTTP call either manually or programmatically. Workers are upserted so that the migration can be run multiple times without creating duplicate records. This template uses batch to efficiently process many records at a time.

![a906a2ec-a05a-4f61-9029-83c700e9a468-image.png](https://exchange2-file-upload-service-kprod.s3.us-east-1.amazonaws.com:443/a906a2ec-a05a-4f61-9029-83c700e9a468-image.png)
<!-- Header (end) -->

# License Agreement
This template is subject to the conditions of the <a href="https://s3.amazonaws.com/templates-examples/AnypointTemplateLicense.pdf">MuleSoft License Agreement</a>. Review the terms of the license before downloading and using this template. You can use this template for free with the Mule Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.
# Use Case
<!-- Use Case (start) -->
As a Workday admin I want to migrate workers to Salesforce.

This template serves as a foundation for the process of migrating Worker from a Workday instance to Salesforce, being able to specify filtering criteria and desired behavior if a user already exists in the destination system.

As implemented, this template leverages the Mule batch module. First the template queries Workday for all the existing active workers that match the filtering criteria. The criteria is based on manipulations starting from a date.
The last step of the Process stage groups the workers and creates them in Salesforce. Finally, during the On Complete stage the template outputs statistics data to the console and sends a notification email with the results of the batch execution. The final report in CSV format is sent to the email addresses you configure in the `mule.*.properties` file.
<!-- Use Case (end) -->

# Considerations
<!-- Default Considerations (start) -->

<!-- Default Considerations (end) -->

<!-- Considerations (start) -->

1. Users cannot be deleted in Salesforce. The only thing to do regarding removing users is to disable or deactivate them, but this won't make the username available for a new user.
2. Each user needs to be associated to a Profile. Salesforce's profiles are what define the permissions a user has for manipulating data and other user functions. Each Salesforce account has its own profiles. This template contains a processor labeled *Prepare Salesforce User* to set your Profile IDs from the source account. Note that for the integration test to run properly, you should change the constant *sfdc.profileId* in *mule.test.properties* to one that's valid in your source organization.
3. Working with sandboxes for the same account. Although each sandbox should be a completely different environment, Usernames cannot be repeated in different sandboxes, that is, if you have a user with username *bob.dylan* in *sandbox A*, you cannot create another user with username *bob.dylan* in *sandbox B*. If you are indeed working with sandboxes for the same Salesforce account you need to map the source username to a different one in a target sandbox, for this purpose, refer to the processor labeled *Prepare Salesforce User*.
4. Workday email uniqueness. The email can be repeated for two or more accounts (or missing). Therefore Workday accounts with duplicate emails are removed from processing.
<!-- Considerations (end) -->

## Salesforce Considerations

- Where can I check that the field configuration for my Salesforce instance is the right one? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US">Salesforce: Checking Field Accessibility for a Particular Field</a>.
- How can I modify the Field Access Settings? See: [Salesforce: Modifying Field Access Settings](https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US "Salesforce: Modifying Field Access Settings").

### As a Data Destination

There are no considerations with using Salesforce as a data destination.

## Workday Considerations

### As a Data Source

There are no considerations with using Workday as a data origin.

# Run it!
Simple steps to get this template running.
<!-- Run it (start) -->
However you run this template, this is an example of the output you see after browsing to the HTTP connector:

```
Batch Process initiated
ID: 6eea3cc6-7c96-11e3-9a65-55f9f3ae584e
Records to Be Processed: 9
Start execution on: Mon Jul 20 18:05:33 GMT-03:00 2019
```
<!-- Run it (end) -->

## Running On Premises
In this section we help you run this template on your computer.
<!-- Running on premise (start) -->

<!-- Running on premise (end) -->

### Where to Download Anypoint Studio and the Mule Runtime
If you are new to Mule, download this software:

+ [Download Anypoint Studio](https://www.mulesoft.com/platform/studio)
+ [Download Mule runtime](https://www.mulesoft.com/lp/dl/mule-esb-enterprise)

**Note:** Anypoint Studio requires JDK 8.
<!-- Where to download (start) -->

<!-- Where to download (end) -->

### Importing a Template into Studio
In Studio, click the Exchange X icon in the upper left of the taskbar, log in with your Anypoint Platform credentials, search for the template, and click Open.
<!-- Importing into Studio (start) -->

<!-- Importing into Studio (end) -->

### Running on Studio
After you import your template into Anypoint Studio, follow these steps to run it:

1. Locate the properties file `mule.dev.properties`, in src/main/resources.
2. Complete all the properties required per the examples in the "Properties to Configure" section.
3. Right click the template project folder.
4. Hover your mouse over `Run as`.
5. Click `Mule Application (configure)`.
6. Inside the dialog, select Environment and set the variable `mule.env` to the value `dev`.
7. Click `Run`.

<!-- Running on Studio (start) -->

<!-- Running on Studio (end) -->

### Running on Mule Standalone
Update the properties in one of the property files, for example in mule.prod.properties, and run your app with a corresponding environment variable. In this example, use `mule.env=prod`.
After this, to trigger the use case you just need to browse to the local HTTP connector with the port you configured in the properties file. If this is, for instance, `9090` browse to: `http://localhost:9090/migrate` and this outputs a summary report and sends it in email.

## Running on CloudHub
When creating your application in CloudHub, go to Runtime Manager > Manage Application > Properties to set the environment variables listed in "Properties to Configure" as well as the mule.env value.
<!-- Running on Cloudhub (start) -->
Once your app is all set up and started, if you choose `wdayworkermigration` asthe domain name to trigger the use case, you just need to browse to `http://wdayworkermigration.cloudhub.io/migrate` and the report is sent to the email configured in the `mule.*.properties` file.
<!-- Running on Cloudhub (end) -->

### Deploying a Template in CloudHub
In Studio, right click your project name in Package Explorer and select Anypoint Platform > Deploy on CloudHub.
<!-- Deploying on Cloudhub (start) -->

<!-- Deploying on Cloudhub (end) -->

## Properties to Configure
To use this template, configure properties such as credentials, configurations, etc.) in the properties file or in CloudHub from Runtime Manager > Manage Application > Properties. The sections that follow list example values.
### Application Configuration
<!-- Application Configuration (start) -->

- http.port `9090`
- migration.startDate `2019-01-28T00:00:00.000+02:00`

#### Workday Connector Configuration

- wday.user `admin`
- wday.password `secret`
- wday.tenant `example_pt1`
- wday.hostname `impl-cc.workday.com`

#### Salesforce Connector

- sfdc.username `user@company.com`
- sfdc.password `secret`
- sfdc.securityToken `1234fdkfdkso20kw2sd`
- sfdc.url `https://login.salesforce.com/services/Soap/u/32.0`
- sfdc.profileId `00e200000015oKFAAY`

- sfdc.localeSidKey `en_US`
- sfdc.languageLocaleKey `en_US`
- sfdc.timeZoneSidKey `America/New_York`
- sfdc.emailEncodingKey `ISO-8859-1`

#### SMTP Services Configuration

- smtp.host `smtp.gmail.com`
- smtp.port `587`
- smtp.user `sender%40gmail.com`
- smtp.password `secret`

#### Email Details

- mail.from `email%40from.com`
- mail.to `email@to.com`
- mail.subject `Users Migration Report`
<!-- Application Configuration (end) -->

# API Calls
<!-- API Calls (start) -->
Salesforce imposes limits on the number of API calls that can be made. Therefore calculating this amount may be an important factor to consider. The template calls to the API can be calculated using the formula:

- *** X + X / 200*** -- Where ***X*** is the number of users to synchronize on each run.
- Divide by ***200*** because by default, users are gathered in groups of 200 for each upsert API call in the commit step. Also consider that this calls are executed repeatedly every polling cycle.

For instance if 10 records are fetched from origin instance, then 11 API calls are made (10 + 1).
<!-- API Calls (end) -->

# Customize It!
This brief guide provides a high level understanding of how this template is built and how you can change it according to your needs. As Mule applications are based on XML files, this page describes the XML files used with this template. More files are available such as test classes and Mule application files, but to keep it simple, we focus on these XML files:

* config.xml
* businessLogic.xml
* endpoints.xml
* errorHandling.xml
<!-- Customize it (start) -->

<!-- Customize it (end) -->

## config.xml
<!-- Default Config XML (start) -->
This file provides the configuration for connectors and configuration properties. Only change this file to make core changes to the connector processing logic. Otherwise, all parameters that can be modified should instead be in a properties file, which is the recommended place to make changes.<!-- Default Config XML (end) -->

<!-- Config XML (start) -->

<!-- Config XML (end) -->

## businessLogic.xml
<!-- Default Business Logic XML (start) -->
The functional aspect of this template is implemented in this XML file, directed by a flow responsible for executing the logic. For the purpose of this template the *mainFlow* just executes the batch job which handles all its logic.
<!-- Default Business Logic XML (end) -->

<!-- Business Logic XML (start) -->

<!-- Business Logic XML (end) -->

## endpoints.xml
<!-- Default Endpoints XML (start) -->
This file provides the inbound and outbound sides of your integration app. This template has only an HTTP connector as the way to trigger the use case.

###  Trigger Flow

**HTTP Connector** - Start Workers synchronization

- `${http.port}` is set as a property to be defined either in a property file or in CloudHub environment variables.
- The path configured by default is `migrate` and you are free to change to one you prefer.
- The host name for all endpoints in your CloudHub configuration should be defined as `localhost`. CloudHub routes requests from your application domain URL to the endpoint.
<!-- Default Endpoints XML (end) -->

<!-- Endpoints XML (start) -->

<!-- Endpoints XML (end) -->

## errorHandling.xml
<!-- Default Error Handling XML (start) -->
This file handles how your integration reacts depending on the different exceptions. This file provides error handling that is referenced by the main flow in the business logic.
<!-- Default Error Handling XML (end) -->

<!-- Error Handling XML (start) -->

<!-- Error Handling XML (end) -->

<!-- Extras (start) -->

<!-- Extras (end) -->

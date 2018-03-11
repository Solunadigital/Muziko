/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.muziko.cloud;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;

import java.io.IOException;
import java.util.logging.Logger;

import static com.muziko.cloud.OfyService.ofy;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "myApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "cloud.muziko.com",
                ownerName = "cloud.muziko.com",
                packagePath = ""
        )
)
public class MyEndpoint {

    private static final Logger log = Logger.getLogger(MyEndpoint.class.getName());

    /**
     * A simple endpoint method that takes a name and says Hi back
     */
    @ApiMethod(name = "sayHi")
    public MyBean sayHi(@Named("name") String name) {
        MyBean response = new MyBean();
        response.setData("Hi, " + name);

        return response;
    }

    @ApiMethod(name = "upload")
    public MyBean fileUploadAction(@Named("url") String url, @Named("user") String user) {
        log.info("upload " + url + " from " + user);
        FileAction existingFileAction = findFileAction(url);
        if (existingFileAction != null && existingFileAction.getActionUser().equals(user) && existingFileAction.getActionTime() + 60000 > System.currentTimeMillis()) {

            existingFileAction.setWaitingUser(user);
            ofy().save().entity(existingFileAction).now();

            MyBean response = new MyBean();
            response.setData(existingFileAction.getAction().name());

            return response;
        }

        // delete expired actions
        if (existingFileAction != null && existingFileAction.getActionTime() + 60000 < System.currentTimeMillis()) {
            ofy().delete().entity(existingFileAction).now();
        }

        FileAction fileAction = new FileAction();
        fileAction.setAction(CommonConstants.CloudFileActions.UPLOAD);
        fileAction.setUrl(url);
        fileAction.setActionUser(user);
        fileAction.setActionTime(System.currentTimeMillis());

        ofy().save().entity(fileAction).now();

//        Sender sender = new Sender(API_KEY);
//        Message msg = new Message.Builder().addData("data", CommonConstants.CloudFileActions.RETRY.name()).build();
//        try {
//            Result result = sender.send(msg, user, 5);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        MyBean response = new MyBean();
        response.setData(CommonConstants.CloudFileActions.CONTINUE.name());

        return response;
    }

    @ApiMethod(name = "download")
    public MyBean fileDownloadAction(@Named("url") String url, @Named("user") String user) {
        log.info("download " + url + " from " + user);
        FileAction existingFileAction = findFileAction(url);
        if (existingFileAction != null && existingFileAction.getActionUser().equals(user) && existingFileAction.getActionTime() + 60000 > System.currentTimeMillis()) {

            existingFileAction.setWaitingUser(user);
            ofy().save().entity(existingFileAction).now();

            MyBean response = new MyBean();
            response.setData(existingFileAction.getAction().name());

            return response;
        }

        // delete expired actions
        if (existingFileAction != null && existingFileAction.getActionTime() + 60000 < System.currentTimeMillis()) {
            ofy().delete().entity(existingFileAction).now();
        }

        FileAction fileAction = new FileAction();
        fileAction.setAction(CommonConstants.CloudFileActions.DOWNLOAD);
        fileAction.setUrl(url);
        fileAction.setActionUser(user);
        fileAction.setActionTime(System.currentTimeMillis());

        ofy().save().entity(fileAction).now();

        MyBean response = new MyBean();
        response.setData(CommonConstants.CloudFileActions.CONTINUE.name());

        return response;
    }

    @ApiMethod(name = "delete")
    public MyBean fileDeleteAction(@Named("url") String url, @Named("user") String user) {
        log.info("delete " + url + " from " + user);
        FileAction existingFileAction = findFileAction(url);
        if (existingFileAction != null && existingFileAction.getActionUser().equals(user) && existingFileAction.getActionTime() + 60000 > System.currentTimeMillis()) {

            existingFileAction.setWaitingUser(user);
            ofy().save().entity(existingFileAction).now();

            MyBean response = new MyBean();
            response.setData(existingFileAction.getAction().name());

            return response;
        }

        // delete expired actions
        if (existingFileAction != null && existingFileAction.getActionTime() + 60000 < System.currentTimeMillis()) {
            ofy().delete().entity(existingFileAction).now();
        }

        FileAction fileAction = new FileAction();
        fileAction.setAction(CommonConstants.CloudFileActions.DELETE);
        fileAction.setUrl(url);
        fileAction.setActionUser(user);
        fileAction.setActionTime(System.currentTimeMillis());

        ofy().save().entity(fileAction).now();

        MyBean response = new MyBean();
        response.setData(CommonConstants.CloudFileActions.CONTINUE.name());
        return response;
    }

    @ApiMethod(name = "complete")
    public MyBean fileCompleteAction(@Named("url") String url, @Named("user") String user) throws IOException {
        log.info("complete " + url + " from " + user);
        FileAction existingFileAction = findFileAction(url);
        if (existingFileAction != null) {

            ofy().delete().entity(existingFileAction).now();

//            String waitingUser = existingFileAction.getWaitingUser();
//            if (!waitingUser.isEmpty()) {
//                Sender sender = new Sender(API_KEY);
//                Message msg = new Message.Builder().addData("message", url).build();
//                RegistrationRecord registrationRecord = ofy().load().type(RegistrationRecord.class).filter("regId", waitingUser).first().now();
//                if (registrationRecord != null)
//                {
//                    Result result = sender.send(msg, registrationRecord.getRegId(), 5);
//                    if (result.getMessageId() != null) {
//                        log.info("Message sent to " + registrationRecord.getRegId());
//                        String canonicalRegId = result.getCanonicalRegistrationId();
//                        if (canonicalRegId != null) {
//                            // if the regId changed, we have to update the datastore
//                            log.info("Registration Id changed for " + waitingUser + " updating to " + canonicalRegId);
//                            registrationRecord.setRegId(canonicalRegId);
//                            ofy().save().entity(registrationRecord).now();
//                        }
//                    } else {
//                        String error = result.getErrorCodeName();
//                        if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
//                            log.warning("Registration Id " + registrationRecord.getRegId() + " no longer registered with GCM, removing from datastore");
//                            // if the device is no longer registered with Gcm, remove it from the datastore
//                            ofy().delete().entity(registrationRecord).now();
//                        } else {
//                            log.warning("Error when sending message : " + error);
//                        }
//                    }
//                }
//
//            }

        }

        MyBean response = new MyBean();
        response.setData(CommonConstants.CloudFileActions.CONTINUE.name());
        return response;
    }

    private FileAction findFileAction(String url) {
        return ofy().load().type(FileAction.class).filter("url", url).first().now();
    }

}

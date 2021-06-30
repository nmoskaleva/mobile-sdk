//
//  CMMessage.m
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/11/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import "CMMessage.h"
#import "CMConnection.h"
#import "MobileSDK.h"

@implementation CMMessage

+ (void)downloadMessages: (NSDictionary*) connection
                 andType: (CMMessageStatusType) type
            andMessageID: (nullable NSString*) messageID
   withCompletionHandler: (ResponseWithArray) completionBlock {
    
    NSString* pwDid = [CMConnection getPwDid: connection[@"serializedConnection"]];
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    NSString* messageType = CMMessageStatusTypeValue(type);
    
    NSLog(@"Connection details %@", connection);

    [sdkApi downloadMessages: messageType uid_s: nil pwdids: pwDid completion: ^(NSError *error, NSString *messages) {
        NSLog(@"Received Messages: %@ for type %@",  messages, messageType);
        NSMutableArray* msgList = [@[] mutableCopy];
        if(messages) {
            NSArray* msgArray = [CMUtilities jsonToArray: messages];
            if(msgArray && [msgArray count] > 0) {
                msgList = msgArray[0][@"msgs"];
            }
        }
        return completionBlock(msgList, error);
    }];
}

+ (void) downloadAllMessages:(ResponseWithArray) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    NSString* messageType = CMMessageStatusTypeValue(Received);

    @try {
        [sdkApi downloadMessages:messageType
                           uid_s:nil
                          pwdids:nil
                      completion:^(NSError *error, NSString *messages) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            };

            NSMutableArray* msgList = [@[] mutableCopy];
            NSArray* messagesArray = [CMUtilities jsonToArray: messages];
            NSLog(@"messagesArray: %@",  messagesArray);

            for (NSInteger i = 0; i < messagesArray.count; i++) {
                NSDictionary *message = messagesArray[i];
                
                NSArray *msgs = [message objectForKey:@"msgs"];
                NSString *pwDid = [message objectForKey:@"pairwiseDID"];
                for (NSInteger j = 0; j < msgs.count; j++) {
                    NSDictionary *msg = msgs[j];
                    NSMutableDictionary *msgDict = [@{} mutableCopy];

                    NSDictionary *payload = [CMUtilities jsonToDictionary:[msg objectForKey:@"decryptedPayload"]];
                    
                    NSDictionary *typeObj = [payload objectForKey:@"@type"];
                    NSString *type = [typeObj objectForKey:@"name"];
                    
                    NSString *uid = [msg objectForKey:@"uid"];
                    NSString *ms = [payload objectForKey:@"@msg"];
                    NSString *status = [msg objectForKey:@"statusCode"];

                    [msgDict setValue:pwDid forKey:@"pwDid"];
                    [msgDict setValue:type forKey:@"type"];
                    [msgDict setValue:uid forKey:@"uid"];
                    [msgDict setValue:ms forKey:@"payload"];
                    [msgDict setValue:status forKey:@"status"];
                    NSLog(@"download mes msgDict: %@",  msgDict);
                    [msgList addObject:msgDict];
                }
            };
            NSLog(@"download all messages result: %@",  msgList);
            
            return completionBlock(msgList, nil);
        }];
    } @catch (NSException *exception) {
        return completionBlock(nil, error);
    }
}

+ (void)waitHandshakeReuse: (ResponseWithBoolean) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    
    @try {
        NSString* messageType = CMMessageStatusTypeValue(Received);
        [sdkApi downloadMessages: messageType
                           uid_s: nil
                          pwdids: nil
                      completion: ^(NSError *error, NSString *messages) {
            NSMutableArray* msgList = [@[] mutableCopy];
            NSLog(@"messages %@", messages);
            if (messages) {
                NSArray* msgArray = [CMUtilities jsonToArray: messages];
                if(msgArray && msgArray.count > 0) {
                    msgList = msgArray[0][@"msgs"];
                    for (int i = 0; i < msgArray.count; i++) {
                        NSDictionary* message = msgList[i];
                        NSLog(@"message when wait %@", message);
                        NSDictionary* payload = [CMUtilities jsonToDictionary:[message objectForKey: @"decryptedPayload"]];
                        NSDictionary *typeObj = [payload objectForKey:@"@type"];
                        NSString *type = [typeObj objectForKey:@"name"];
                        if ([type  isEqual: @"handshake-reuse-accepted"]) {
                            return completionBlock(true, nil);
                        }
                    }
                }
            }
        }];
    } @catch (NSException *exception) {
        return completionBlock(false, error);
    }
}

+(CMMessageType) typeEnum: (NSString *)type {
    NSArray* types = @[@"credOffer"];
    if(![types containsObject: type]) {
        NSLog(@"Invalid type provided");
        return Credential;
    }
    return (int)[types indexOfObject: type];
}

@end

//
//  CMCredential.m
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/11/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CMCredential.h"
#import "MobileSDK.h"
#import "CMMessage.h"
#import "CMConnection.h"

@implementation CMCredential

+ (void)acceptCredOffer: (NSDictionary*) messageObj forConnection: (NSDictionary*) connection withCompletionHandler: (ResponseBlock) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    
    @try {
        NSLog(@"Received Cred Offer to process - %@", messageObj);
        NSString *messageId = messageObj[@"uid"];
        NSString* pw_did = [CMConnection getPwDid: connection[@"serializedConnection"]];
        
        [sdkApi connectionDeserialize: connection[@"serializedConnection"]
                           completion: ^(NSError *error, NSInteger connectionHandle) {
            if (error != nil && error.code != 0) {
                completionBlock(nil, error);
                return;
            }
            [sdkApi credentialCreateWithMsgid: messageId
                             connectionHandle: (int)connectionHandle
                                        msgId: messageId
                                   completion: ^(NSError *error, NSInteger credentialHandle, NSString* credentialOffer) {
                if (error != nil && error.code != 0) {
                    completionBlock(nil, error);
                    return;
                }
                
                [sdkApi credentialSendRequest: credentialHandle
                             connectionHandle: (int)connectionHandle
                                paymentHandle: 0
                                   completion: ^(NSError *error) {
                    if (error != nil && error.code != 0) {
                        completionBlock(nil, error);
                        return;
                    }
                    
                    [sdkApi updateMessages: @"MS-106"
                                pwdidsJson: [NSString stringWithFormat: @"[{\"pairwiseDID\":\"%@\",\"uids\":[\"%@\"]}]", pw_did, messageId]
                                completion: ^(NSError *error) {
                        if (error != nil && error.code !=0) {
                            NSLog(@"Error occured while updating message status - %@ :: %ld", error, (long)error.code);
                        }
                        NSLog(@"Updated messages for message: %@ and credentialHandle: %ld", messageId, (long)credentialHandle);
                    }];
                    
                    // start a loop and wait for actual credential to be issued
                    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                        while(true) {
                            dispatch_semaphore_t acceptedWaitSemaphore = dispatch_semaphore_create(0);
                            __block NSInteger credentialState = 0;
                            
                            [sdkApi credentialGetState:credentialHandle
                                            completion:^(NSError *error, NSInteger state) {
                                if (error && error.code > 0) {
                                    dispatch_semaphore_signal(acceptedWaitSemaphore);
                                    return completionBlock(nil, error);
                                }
                                
                                if (state != 4) {
                                    [sdkApi credentialUpdateState:credentialHandle
                                                       completion:^(NSError *error, NSInteger state) {
                                        if (error && error.code > 0) {
                                            dispatch_semaphore_signal(acceptedWaitSemaphore);
                                            return completionBlock(nil, error);
                                        }
                                        
                                        credentialState = state;
                                        dispatch_semaphore_signal(acceptedWaitSemaphore);
                                    }];
                                } else {
                                    credentialState = state;
                                    dispatch_semaphore_signal(acceptedWaitSemaphore);
                                }
                            }];
                            
                            dispatch_semaphore_wait(acceptedWaitSemaphore, DISPATCH_TIME_FOREVER);
                            if (credentialState == 4) {
                                NSLog(@"credential accepted");
                                break;
                            }
                        }
                    });
                }];
            }];
        }];
        
    } @catch (NSException *exception) {
        return completionBlock(nil, error);
    }
}

@end
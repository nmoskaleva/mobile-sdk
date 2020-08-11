//
//  CMProofRequest.h
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/18/20.
//  Copyright © 2020 Norman Jarvis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CMUtilities.h"

NS_ASSUME_NONNULL_BEGIN

@interface CMProofRequest: NSObject

/*
    ProofAttributes dictionary should contain 2 keys: autofilledAttributes & selfAttestedAttributes
    autofilledAttributes: attributes retreived from existing credentials (most of the attributes will be this type)
    selfAttestedAttributes: attributes which user will need to fill in UI form
 */
+ (void) sendProofRequest: (NSDictionary*) proofObject proofAttributes: (NSDictionary*) proofAttributes andConnection: (NSDictionary*) connection withCompletionHandler: (ResponseBlock) completionBlock;

+ (void) autofillAttributes: (NSDictionary*) proofObject andConnection: (NSDictionary*) connection withCompletionHandler: (ResponseWithObject) completionBlock;

@end

NS_ASSUME_NONNULL_END

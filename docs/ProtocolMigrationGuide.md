## Developer Guide for MSDK migration from protocol type 3.0 to 4.0

This document is written for developers who started using MSDK with setting `protocol_type:3.0` and now want to upgrade their application to use the latest MSDK build supporting `protocol_type:4.0`.

### Reason to upgrade

If your application is using protocol type `3.0` and now you want to support [Connection Invitations with Attachment](./9.Connection-Invitations-With-Attachment.md) containing Credential Offer / Proof Request, you will have to support two different formats of `Credential Offer`/`Proof Request` messages because:
*  for protocol type `3.0` all functions return messages (`Credential Offer`, `Proof Request`, `Credential`) in the custom legacy format compatible with protocol types `1.0` and `2.0`. This protocols are not used anymore.
* [Connection Invitations with Attachment](./9.Connection-Invitations-With-Attachment.md) contain `Credential Offer`/`Proof Request` formatted in modern Aries way.

In order to avoid supporting two message formats, you should update your application to use protocol type `4.0`.

### Differences in protocol type `4.0` compared to `3.0`

* all functions return messages (`Credential Offer`, `Proof Request`, `Credential`) in the `Aries` message format instead of legacy one.
* changed the format of result value for function to receive credentials for a Proof Request `proofRetrieveCredentials` function ( will be explained below).

### Credential Offer

See [Credentials document](6.Credentials.md) which currently reflects steps of accepting a Credential Offer with SDK configured to use protocol type `4.0`.

* **3.0 Credential Offer Message Example**

    ```
    [
        {
            "claim_id": "123",
            "claim_name": "DEMO-Transcript",
            "cred_def_id": "R9kuPrDFDVKNRL1oFpRxg2:3:CL:33627:tag1",
            "credential_attrs": {
                "DEMO-College Name": "Faber College",
                "DEMO-Degree": "Computer Science",
                "DEMO-GPA": "4.0",
                "DEMO-Major": "SSI Software Engineering",
                "DEMO-Student Name": "Alice Andersen"
            },
            "from_did": "Byyf6DiChBaukbgPssuFi1",
            "libindy_offer": "{\"schema_id\":\"R9kuPrDFDVKNRL1oFpRxg2:2:DEMO-Transcript:1.0\",\"cred_def_id\":\"R9kuPrDFDVKNRL1oFpRxg2:3:CL:33627:tag1\",\"key_correctness_proof\":{\"c\":\"18409263165024561057868297548338734429203182181892601294150224288240356148969\",\"xz_cap\":\"89092862071754482768568121750605441740780661720589296980571662371912825324849589562417824532135192968565372473462126811235969943884283863879335732191646688609906727685365837864115018709597962744791616448487915667192875058592815636339507614499227407943494357843528056033773721711029552842160396617016884835556020495082061907517897139296921868726098793929456464992060745065258560960963184459787524683244617708108982997614880502648066019837184627185880652671440406751004823716138038578482286313872937436689907376336392198624082502390842170298696350711224912393805607280115907250438353352931044945179040370553648653866272291219097868230012644023837579478565817467758686729145484709030823195983554\",\"xr_cap\":[[\"demo-major\",\"164587753942899494168745746702534281563037669288218682692257391165325041732168189460545115812308404279240589114425046322356132181408031051286239643169009005958223435439799355211662444104531883082538176014292077920846413938760235906086381910034356130381286749206128094454452662283815044330730800323967902470312775790455241239653482745004223850680770052818976351902205471198454535294783397658320491100845383694178245356320509202610422971809318360852346231110471745351684915383461541209210464090117212587703482762934418809859227396852297417037033140072272676151762891624090259458948049903044932665550956351020903956472199708780575514718753305756346547634669337342447465198642113593266941556003489\"],[\"demo-gpa\",\"22199559154241071233540251324195337230927822968669546682924256492487853877953031345825385745900241025330375871523917849712725385790432951076987595680053019446054741259892564701293623250303595931063343966034351309995316956097733763166994791887902101149798874562034383975331980161354768409446422855147483195212437317537973845251280883872998417723398849872761881339298451486684674885318327796748378944659613162839511183514127487736387801234622230589841243519960874429783488747835807721882053729686382271314635805402653965411193898033208899024711454939860531264594452160431601266653407877024382554166965954564507888453238080543443825294612573362378277653648119231853727251371252426611250719223027\"],[\"demo-degree\",\"425307926357731961641889723351192532598285842402504657385351442966147456820187648192467747579412990962082780672922537000698541127067274541764366465881608778619655051450139613208362852093703153304455076974106861473420212168528190233224128135187035013850265971419193482519553936471029341985892219443902953021308950958867128132854325602761697845595230964616277064461362750199729352648568624273623621409353173348918863292813264771105478518129083486751236120470941869433608154015175461988708774026503367528484264960283828722023093081762732688330723751523175400425861380454412846687704806799430524033550455771282169951369159625112011956983708855437333914168956346464882619206522338259213757536326058\"],[\"demo-studentname\",\"332942112992078956065502211608387712655001083010169952511826075054945177324008673179434276989273295506738415570486847706494046829912089490672907344863578560956535931875873789276035124529730434622632506113551817581613699326755751216014910566217111708516008633400092766396982417384090165975121601372757060655383618934195784794468382484016906065941618773224768793098806073747798898613543519044035259337593748741589107633738885055731088913891966617458130135496565870795773171361810258169268160133602228280475389510872612504749862640093542170867364327914094068393807397625776548483684493102676731111623554447585743020640035792033063140954939015249185968692270629089072758373236129352580683143071761\"],[\"demo-collegename\",\"256956542286851333861178542347550843546602336608011961558790339355582543368619947132431033284018607829183717192553026370913923276337100278000711086078700912704406969815450902416202898668180355104154375118864951703873631679369560591960412167087463159579554031232611474483835459581596891709629200793913448526516843429369270281150318082896345684610971073438995144135178721242301138106737185350870630144213892199456597667151448810510989482504838867314779162444998243004059058257958923094769333676695275088360084008223593237541561581754838827142975493992967377714562756829715354497023402091089159902814924453530031297226196362145195501696869223672653009915485152284531305174191275718734671168638531\"],[\"master_secret\",\"411035279220097313141639496560361556200182820268236744181042170495644257271602560662636267278449740845231099678615487666867274088697078282727112089775108679200935406965394662679949604744630828792989232095978841363694220200918070829219562333021609323718741068752958915034422243090830005990644002315152710000100339840653484954546420674392862441143898339041785588558280237678400651047112646434338047479802903948283008613054301117081310752384053946023208896128041360508717835899081555400948972385606201372740711885971254389857009228358812537741320250162116765962040122767139702034734251663243075544887564157715654976094619015563754259692900247899890694680086596189292951372041405036180254469521378\"]]},\"nonce\":\"451547265861164114557369\"}",
            "msg_ref_id": "b060ca52-873e-4606-bf49-8ba83e4320a0",
            "msg_type": "CRED_OFFER",
            "schema_seq_no": 0,
            "thread_id": "467f6449-7d1f-4a9f-ada7-09d6444af083",
            "to_did": "Byyf6DiChBaukbgPssuFi1",
            "version": "0.1"
        }
    ]
    
    ```
       
* **4.0 Credential Offer Message example**
     ```
     {
        "@id":"6269f643-beb2-4e98-92f1-b9683fa1cb36",
        "@type":"did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/issue-credential/1.0/offer-credential",
        "comment":"DEMO-Transcript",
        "credential_preview":{
           "@type":"did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/issue-credential/1.0/credential-preview",
           "attributes":[
              {
                 "name":"DEMO-College Name",
                 "value":"Faber College"
              },
              {
                 "name":"DEMO-Degree",
                 "value":"Computer Science"
              },
              {
                 "name":"DEMO-GPA",
                 "value":"4.0"
              },
              {
                 "name":"DEMO-Major",
                 "value":"SSI Software Engineering"
              },
              {
                 "name":"DEMO-Student Name",
                 "value":"Alice Andersen"
              }
           ]
        },
        "offers~attach":[
           {
              "@id":"libindy-cred-offer-0",
              "data":{
                 "base64":"eyJzY2hlbWFfaWQiOiJMblhSMXJQbm5jVFBadlJkbUpLaEpROjI6REVNTy1UcmFuc2NyaXB0OjYyLjIzLjQ3IiwiY3JlZF9kZWZfaWQiOiJMblhSMXJQbm5jVFBadlJkbUpLaEpROjM6Q0w6MjcwNzA4OnRhZyIsImtleV9jb3JyZWN0bmVzc19wcm9vZiI6eyJjIjoiOTcyNDEyMDUwMDE0MTM4NDUwMjY4MzQ4OTU0MDA5NzEwNzgzNzcyMzE2MjcxODUyNDYzMjkyMTE5MzAxOTY2MjU0OTI1MTA5MzAzNzgiLCJ4el9jYXAiOiIxODc3MzMwNDEwNzM0NjA4MjQ0NzQ4OTIzMjMwNzk4MDE5NzgwNTI3MDM2MTQ2MzIwMzM3MDQ5MzQxMjkzMTU4MTY2NDUzMTg0MjAzMjUyOTU2Mzc1MzM3OTI5OTIwMzgxMDE1OTI5MTI0ODA2MjUxMjM3ODYyNTEzMjIyMDkyODc5OTI2MzM3Njg5MDY0NTI1NjAwNTYxMDE1ODk0MDc5NTk5MzM2OTcyNDE0MjI1NDIwMzQ1MTM5NTg5Njc5NzU3NjU5NjQ5MDYwMDM1NjkyMzY3MTgwNjA1NjQ0Mzk3NTYxNjA1MTY5NjU3NTA3MjE0OTEyMzkwMTQ5Njg2NDY3MTEzMTAzMjE0MDQxMDIwNDAzNjMwMzQ0NjI4MDE1NzkyMjQ1MDMzNDM3ODkwMzcwMzE0MzIxODc4NzgzMDA1NjE2NDQ5NTg3ODUwNDI3MTg4NzgyODQ3ODg4MjM1MzEwNDExNjA4NDY3NzUxMjkxMzY5NjE2OTgwMzE2MzcyODA2MDk5NzU0NzMyODY0NTM4Njg3MTUwOTM2NDI4NTQ3MDc2MTQ1NTY2NzIzNTU4MjI2OTIwMzE4MTA4NzQ5NjQyMjkzMDQwNTIyODA2MjExNzM2NTA4NzU4MTU2NzM5NTA4NjM5NDc5NDA0MTY2NTk5OTc2OTU3MzAyMzQ0OTQ2NjAyNTM5NTE4OTM2MzgxNDE2Mjk0NDk1NzExMjg2NjEwMjk0NTk0MTEyNzk3NzMxNDc0NzQwMDQ1NTYyNjUxNDQ1NzE1MTUxODYwNTAyNTkwNjk3NDcxNzczODI5MDg5OTI1NzM4OTI3OTUyNDkxMjg3NjEwNDgyNzQ5NTI4OTAxNDc4MzI3NzIwNDI5MDY1OTY4MjYwNTk3NTU4NTI4ODI4MTgyNjYzOTI2MDUwNjA2NTk2MzE2IiwieHJfY2FwIjpbWyJhZ2UiLCI2NjM3MzcyMzI4NDIwNjAxMTQ2ODc0OTcyNjU4MjIyOTY5NjIwMDM0MTY5NjMwMDA2MDg3NDUyOTU5NTg3MjM2MjExMzkwMjgwNTM1OTgzNTU0ODc4OTMxMTI2MTk1NjMzNTM3NDIxNTU3ODg0MTM1NDIxMDE2OTIyMzE0MzQ2MjE3NDE1Mzg0OTM1MzMzMjg2NTM5ODc4NTEyOTMxMDU2MjkyMjI1MDA2OTg5NDM5Mzc0OTM4ODMwNzc5ODE4NzczMTM4NDMyMjU2MzY0MzA5MjQyMjU4NzExMjgwODQwOTI3NzI2Nzg1MjM2MDg0Mjc1NzM1NjU1Mjc1OTU1OTc1NzkxMDczOTUzNDk4NTkwOTU4Mjk3ODM2MTExNjYxNzY3NDg3OTA4OTI2OTgwMzA2NzAyMTAxNzU1MDUwNjQzODMzNjc0MzQ0NDgxNzY2MzE1MDI4Mzg4NzQyMTE3MTI3NjI5MjczMTYwMTQyNjk1MjM5NDIyNDc2MzY2OTQyMjIzMjg0MTEzNDYxOTU4NTE3OTI4MjczMzgxNjE1NzM1MzY2NjQ4NTA0MTQwNTc5NDI2OTI2NDg1OTQzODUyODA0NjIyMzAxMDk4MDc5OTIzMTczOTAyNTA3NTE2MzYzNzA5NDM3MjU5NDQxMzY0OTU0OTE1NjAzNjUyMDE3OTk5MTUxNjk4NTkwMDcwNTM1OTUzNzE5MjkwOTI2NjEzNjIyNzI0MzYxNjA1OTg5NzU0Njc1MTUxNTEyNjMwNzE5NDU0MDMwNjcwNDkwMzE1MTUzOTE2Mzk2MTUzNTIzMTU0NTMxNjQ2OTc1MTYzNzc3OTY0Njg0ODYzODIwNjQ3NzUyMTE5NDU4NjkyNTE3Nzg2NDMwNzYxMjk2NDY3MjgxODQ2MTgzMjQzNTg3NjE1MjIzNTU2NDYiXSxbIm1lbWJlcmlkIiwiMTgyMjMyMDc4ODMxODEyNDc3NDM3NjE1NzU3ODgwMTIxNzA2MjIyMDYxNTE4MzMwMjI3NTE5NzUwMjEzMzk2OTYxMzE0MTU2OTU3OTg5MjQwNzUzNjQwMDQxNDc4MDYwOTE2MzAzNzI3MzQ4NTU1ODgzMjE2MzM0NTMxMzMzMjI1NzA3ODg5NDgyMzM5MTQ5NTMyMDAxMjA0MjM1OTg0NzAzNTAzOTA4OTg0MDc2NTE4MjY0Njg5ODAxMTkwMDc3MzY0MjQ4ODg1NTE2MDc0NTI0NTg4MjMwMTQ2NDI5MDIyOTgxMzIwNjQ1NDQ1Mjg2MTk3NDkyNDU1MDU1NjEwOTY3MTcxMzQxMzExMjg1NDY0NjY1NzYxODE2MTI3MDE0MjA3MzY0ODU1Mzg2OTYxODUwNzY5ODE3OTE2OTE2NTI5Njc0MjQ5NjY1MDc0MDI0MjgxMzMwMTE4NjAwMjQzOTM0NDk0MjQxMTM1MzI0MDQ5ODUxNDQwNjg5MzkxNDQxODI1NTc5Mzc4MjA3MDM2ODUyMzM5MzMxMTAxMjU1OTcwNjU2MDcxNDc5Mjc1ODQ1ODY0OTY1OTk4ODczMDQ0NjM3MTQ2MTY0MzEyMzMxMjIwNjExNDEwOTg1MDg0MDI3MTg4MDg5NzI1NzQxMDY1MDAyODg0NTQ4ODYzMTE3ODA2Mzk4MjMyNzA5MDQ5NzYwMjQ1MTQwNzI1NzEwNTM3NDExOTEzODE0NjkyMDk2MDExOTQ4ODc4MzE1NDA0MTc0MzM3NTg0MDY0MTg4NzYxMTgxMDU5NzY3MjIwNjk3MzA2MTMwMjEyMTE4MTM2NDc1OTQwMTg1OTA1NDczODMwNjUyOTc1NTM4NTY5MDUyODA2NTcwMjI2NDk3MzA5ODkzMzI3NjcwNjY3MDk4NzE4MTAzOTY0MyJdLFsic2V4IiwiMjAxMDExNTg1NDczNjY3MTYzNjMzOTc4MTg4MjA0MjgwNDM2MTI3NTM5OTYxNTA2NTk3MTYwNjUwMDY1NjMwNTg4NDQ4MTk0MDI4NTUyNDc5MDI1NTU5NzUxMjQ0NTY2NzA5ODIxMTU2NTUyMzIyNTgzMzYwNTUxNjg1MDY5Mzg3NzY1NTM2NjEzMDgwOTEwODE2NDA4NjgyNjUzNDc1MzE0MDAyNzM5MzM2MzY2MTM1Mzk2OTgzMDY1OTA4NjIyNjczMDEzOTM5NjUzOTk2OTc1NjE5Nzg1MjA0MzQzNzE4NjE3MjM3NzI3NTkyNDcyMDIxNjg3MDQ5ODU5MTQzODI0NjU1MTg2MzE2OTMxNjI4Mjc4NDg5MzYzNDAyMzQ0NDkzNDM2MTU1MzA3NTAxNjg2Njk2NjMwMzE4MTQxNDAxMDM3MjAyNzA2MDEwMzk1MDI3NjU3MjE3NDMzMjgwMDAyMjc4NTg3OTA1NTMxNTcxMjM2MTM5OTk2OTU5ODE2NjE1Mzg0NjU2OTM3NjA5NDE5MzExNzAzMTI1Mjk5Nzc4NDMwNzQyMjg5OTU1NzUwMzU5NjY1MjE0MzM4NzM5Nzg5MzAzNDIxOTY0MjIwMTAyMDI4MTkzMDgwMzk1OTg3NjEwOTQ5ODIzNDYyMTQ5NDkyMjgwNjkwNzk3MjU4ODc4NjQ5MjU5NTY3MDUyOTMyMjQzOTIyODgwNDE2ODI4ODQ0MzMyOTg0NjAyMjcxODkwODk3NjA0OTUyMDUzMjg2Mjk2MzEyMjU3MDQ4MDY3MzAyMzAxMzYwMjMxMTM1OTk3MDc3NDk2NzY5MzU5NzA0MDM0NjA1NjY0NzgzMTg2Njc4NDMxMDE0NzQ0MTEwODUwMTk0NjA3ODc5MjkxNzAyNjIzNDc0MDMwOTQ3OTU1MjAzNjk0MiJdLFsiZmlyc3RuYW1lIiwiMTMzNzE1MzcwMjA1MTU4NTc0NDYzODYzMjE2NjEzNjI0OTcxMTg3ODUwMjczNzUwNTYyMDkwMTg0MTc4OTAzOTM4Mjk3NDUxNjQ2NjEwNzQxNDU1MjA0NTY1NDU0NzU5ODMwMzgzMDk1MTg1MDI3MDY0Mzc4NzE5NzMwMTM3NjE5OTA2MDA5ODczMjA3Mjg0MDYyNjU5MjQzMjAzNzczMzQ3MTU2Njk5ODAwOTA4ODc4OTgyMDMxNzM0Mzc4MTUyNTc1MzA3MjkxNDUyNzAzMTkzNDI3Nzg0NDU4OTY3Njk4OTgyODU4MTE3MzAxNTQxNjU0NTkwMTY5NDY5Mjg1OTk2NjY5MDg0MjMxMDczMzc0MzM2OTU5MzM4Nzg5MDYxODMzMDg4OTA1NTQyNTMyMDI0MDYyNzI3NTk4MzM0MjUxMzA4OTQxOTcyNDM1ODQ4MDExNTQxMjg4MzU0NDk3MzE0NDM5MjY2ODQxMTcwOTU5MDg0OTg2ODYwODYyOTQ0MDY3ODY4MjE2NjI4OTQ1MzcyOTEyMTkzMjA3OTQ4Mzg5ODI0MTk4MjExNDc5Njc1NzA3OTU3MTU2MTAyNzg2NDUyOTk5Mjc3MTc3OTIzOTkxMDgyMjUzNDQ3NTMxNjQ5OTYzNDE0Nzc5ODg2MDgyMjk3OTk1ODg1MTczNzQzODY0MDc1NzI2MzI3NDc4NDYyNzE2NDY3ODg0NzYxNTA3MDM5NzAwOTQ1NDIzNzQ0MTA5ODI0MDA0MDEzMTkyNTQ0NjM4NTY4MzM3MzMwMDA4MDY4NDIxMjY0MTk2ODk0OTc2NTk1ODMyODc3ODQ3ODU2NDg3OTU4ODMxMTkwODQwODM2OTEzMDI1MDUwMjY2NDk2Njg3NzYzMzMwNDgyMDEwMTA0ODIyMjMwOTU5NzAxNTU3NDUzNiJdLFsic2FsYXJ5IiwiMTAyNjAyOTA4NzM4NjU4Nzc2MzY5NTg1Njc5OTUxMjM1MzUyNDUwMzM1MDA5NTIyMzY3Mjk2MDc0MTQ0ODcxODYyOTI4MjY3NDM5NTA1Nzk2MDU3MzI4MDU3MDYzODQyMjQzODEyNDkzODY1Mjc4NDY2MzM3NzI3NTk0MzIzODE2NzY1NTI5MTAxNDE1MDQ2NTIzOTg1NzU4ODUxNTkzMzU1NjU4Njk5NTA5MTUxMzM1ODI0Mjk1OTg1Nzg3OTc2NzEyOTkxNTAyNDE4ODQyNzk3MzU5NjM1MjM3Nzk1NzE5ODIzODExMjM3MTY0MzM1NDM5NDI5NDg4OTc3NjcyMjE1MzM4MDgwOTg0MjQ1MDUzNjgwNzYxNzQ0NTg5OTk2MjAzNzQ0MDIyMzE5NDA4NDgwMDA1Mzc5OTIwMjU2MzU3ODcxNTYzMzM5NTU2MzA1Njk5ODQzNzY0MzgxNDQzNDg3ODA5MDU0OTcyNDE0NjUzNTM0NTc1MjkwMTE1NDkwNDEwNzE4NzQ5MjM3NzY4MjQwNDEzMjc1MzM4MjUxMDYxMzIzNzcwMjkzNzg3ODY0ODgzNzcyMjc5NzAzOTA0MzAwOTQzNDI2NjM4NzQ0ODU1MTkyNjQwMzk1MDU3NzE0MDAyODk5NDgyNzkyMDcwODg5NjMzOTE4MDMyNDU4MzcxNzYwNDI4NzgxMTc5MzM0NDA3ODk5Nzg2NDg2MjU2MTU5MzY3OTA0MzUwOTM4NTgwMTg1MTkxOTg0MzU5NTc5Mzk0NDkwNzU2Mzg2NjU4NTUzOTI5NTkzNzYyODI2MTkyOTE0MTkyOTcwNzcxMDY2MTA0MTk2ODMyODAyMjg0MTAwMDc0ODAwODQ2NTcyODg3NDMwODExMjU3MjI5MDU3NzAwNDE0NTUzODE4OTc2MjQ4MzM2NyJdLFsibWFzdGVyX3NlY3JldCIsIjE5MTc1MjU2MTcxNzk1MTE4OTM2MzYxMDkyOTU5MDYyMDQzOTkxOTA3MTczNjE0OTM2MjY1ODI3MzExMzU2NzU1OTAyMDQ3NDM4ODA3Nzc2MzIyMTkzMzk4MDc3MDE0MDUwNTkyNDU4NzEzMDkyMzgwNTgyNTQzODgwODk3MzkyNTg3NDkwNDUxNjM1MjM5MDQ5OTA0ODI2NTEzOTUzODE1NDY0NTEwNDU1MDQwNTUwNTg3MTY2NTgwNzgyMzA4NjU5ODQxMzE2NzAwOTA1MTU3OTU2NjQ4MDY4MjA3MjY0MDc2NTM1NTIxMTE0NDUyOTg5ODk5NzM3MTcwNjkzNDI3NzQyODc5NDIzNjI4NTMxOTM2OTYzNjMwNzA4NDc2MzQ2ODY2Nzg3Mjk5NjQ2ODUyMDE5NDQ4MjgyMTUxMjU3OTA0NTk5NDQzODE3OTYwMjAzMzk0MTk0MTUxODY5Njk1NDI4NzA1Mzg3OTQ0NDQ0NTE3MjU0ODE2MTkwNDY1MTk5MzgyNzI1OTU5MTAzNzY1MTEwMTg5MTQxNzgyMzk0NTk0MjU1ODU3OTkxOTUwNzAwMDg1ODcxOTQ4OTEyMDY3MzMxNDE2MzQ4NTE3NjQyNDIyMTIxMDQ1ODUxMDg5NTIxNDA5NzMwNTM2NjE1MzAwOTEzNTY4OTUwMDUwODY4NDMzNDc4MzY5NjAwODAxNTkyNzY0ODg1MTkwMDQzNzMzNjk3MTM2MTMwMTQ5NzUzMDIxMDEyNjg2MTY0MzAwOTM4OTg5MDE2NzI4NTYwNzQ3MzQ4NzkxNDAwNjAzMTkyOTI3MzUzNzc3NjkzODk4MzAzOTI0OTU3ODY5NzgyNTU4MzI1ODM2NzE1MjM4MTQ5NDkxNDA0NDI5NTI0MDM5OTMzODgxNTUyMTAzMjEwNjUzNzE0OTgiXSxbImxhc3RuYW1lIiwiMTcwMjM1NzU3MDEwODIzNDA2NzYzNTY1NTAxNzY1NDg4MTE1MzAzNzkxNTk1NTk0NTAyNjcxOTg1ODU3MjY3NjY2MzEzNjE3MDkyNDgzMTIyODAwMzE0NzUyMjYyMjA1NzE1NTg4MzEzNDg4NTQ3NzQ1Mzk3MzAxMjE0NDU5NTI2MDA3Nzk1NjM2NDAxMzU5OTAzMDM0NzMzODEwOTA5NzgzNzQ0MzA4ODI0OTg3NTU5NDQyNjE0MTk3ODgwOTE1NDYyNzAwMTM1MjU0ODQyMDYzOTU0Mzg1OTcwMzY4NDQ1NzM5MTQ4OTMyMjYyNTkwNjQ1NjM2MDYxMDMwMjgyNTY5MjUxMDMzMjExMzE0NTk0NTQ1ODk3NjAwMDQ2MTU3Mzg3MDg0NTM5ODE1NTkyODU1NDYxNDA2Nzg1Njc5ODY0NTYwODQyOTMwMDQwNzg2MjY3ODY3ODMzMjYzMjA0OTcyMDI3NzcxMjcyOTk5MDc5MTU1NjE4OTMxMjI1MjcwOTE2OTU3MTEwNDQwMDA0NDA5MDI4MzA0MTAxNTk5MjYwNDYxMzQ5MTk1MDkxOTAzNjY5ODA0NzE3ODMzODE0NjQwNzU5OTM5ODI5NDY3MTc4ODkwOTEyMTMzNjU5ODIyMjg5NjA5NjMwNzk3Mjg2NzAxMDUyOTA2MDU4NTk4MTE5MTgxMzg1ODc2MDE2NDMyOTM0MTc5NzUzNDA2MjY0MzQzOTM4NTIwOTU1ODk0MTAwNjk3MTgxOTY1Mjg3NzkxMjYzMjkzNTY4MDIxNzAzNjU5NjU1OTQzNDIxNDY0NTQ4NzE1MDU1MTg0MjUwMDk3MDk1NTQ4NDI4MTIxODgxNzczOTI2MzM3NDMyMTE5NzU0NTI5ODMxMDE5MTI1MDM2NDU2Njc1Mzg4Nzg0MzY2MjU0OTA2NCJdXX0sIm5vbmNlIjoiNzE1MjI4ODAyOTM3MzkwNTU1MzA3NzEzIn0="
              },
              "mime-type":"application/json"
           }
        ],
        "~thread": { // (Optional) maybe omitted
           "thid": string, 
        }  
     }
        
     ```

### Proof Request

See [Proofs document](7.Proofs.md) which currently reflects steps of sharing a Proof with SDK configured to use protocol type `4.0`.

* **3.0 Proof Request Message example**
     ```
     {
         "@type": {
             "name": "PROOF_REQUEST",
             "version": "1.0"
         },
         "@topic": {
             "mid": 0,
             "tid": 0
         },
         "proof_request_data": {
             "nonce": "220867029780621153091790",
             "name": "Basic Info",
             "version": "0.1",
             "requested_attributes": {
                 "attribute_1": {
                     "name": "Number"
                 },
                 "attribute_2": {
                     "name": "First Name"
                 }
             },
             "requested_predicates": {
                 "predicate_1": {
                     "name": "Age",
                     "p_type": ">=",
                     "p_value": 20
                 }
             },
             "non_revoked": null
         },
         "msg_ref_id": null,
         "from_timestamp": null,
         "to_timestamp": null,
         "thread_id": "s2g2b311-ss21-4a9f-ada7-09d6444af083",
     }
        
     ```
   
* **4.0 Proof Request Message example**
     ```
     {
        "@id":"d7f98364-2995-413d-8d20-ee1c817e1dd2",
        "@type":"did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/present-proof/1.0/request-presentation",
        "comment":"Basic Info",
        "request_presentations~attach":[
       "~thread": { // (Optional) maybe omitted
           "thid": string,   
        }  
     }
           {
              "@id":"libindy-request-presentation-0",
              "data":{
                 "base64":"eyJuYW1lIjoiQmFzaWMgSW5mbyIsIm5vbl9yZXZva2VkIjpudWxsLCJub25jZSI6IjIyMDg2NzAyOTc4MDYyMTE1MzA5MTc5MCIsInJlcXVlc3RlZF9hdHRyaWJ1dGVzIjp7ImF0dHJpYnV0ZV8xIjp7Im5hbWUiOiJOdW1iZXIifSwiYXR0cmlidXRlXzIiOnsibmFtZSI6IkZpcnN0IE5hbWUifX0sInJlcXVlc3RlZF9wcmVkaWNhdGVzIjp7InByZWRpY2F0ZV8xIjp7Im5hbWUiOiJBZ2UiLCJwX3R5cGUiOiI-PSIsInBfdmFsdWUiOjIwfX0sInZlciI6IjEuMCIsInZlcnNpb24iOiIxLjAifQ"
              },
              "mime-type":"application/json"
           }
        ],
         
     ```
     
     > Use `extractAttachedMessage` function to get requested attributes and predicates (`proof_request_data`) from proof request message.

### Credentials For Proof Request

For protocol type `4.0` the format of resulting value for the function to get credentials `proofRetrieveCredentials` for a Proof Request changed to return more information for each requested attribute and predicate:
  * values of requested attributes fetched from credential.
  * if an attribute can be self-attested
  * if an attribute is missing
  

* **Result for Protocol Type 1.0, 2.0, 3.0**
```
{
   "attrs":{
      "attribute_0":[
         {
            "requested_attributes":{ // requested attributes
               "MemberID":"435345"
            },
            "cred_info":{
               "attrs":{
                  "Age":"27",
                  "FirstName":"Rebecca",
                  "Lastname":"Greaves",
                  "MemberID":"435345",
                  "Salary":"1000",
                  "Sex":"Male"
               },
               "cred_def_id":"LnXR1rPnncTPZvRdmJKhJQ:3:CL:273930:tag",
               "referent":"67bf1885-5f6d-467d-80b1-1e3c0c4bee5f",
               "schema_id":"LnXR1rPnncTPZvRdmJKhJQ:2:degree schema:68.21.36"
            },
            "interval":"None"
         }
      ],
      "predicate_0":[
         {
            "requested_attributes":{ // requested attributes
               "Age":"27"
            },
            "cred_info":{
               "attrs":{
                  "Age":"27",
                  "FirstName":"Rebecca",
                  "Lastname":"Greaves",
                  "MemberID":"435345",
                  "Salary":"1000",
                  "Sex":"Male"
               },
               "cred_def_id":"LnXR1rPnncTPZvRdmJKhJQ:3:CL:273930:tag",
               "referent":"67bf1885-5f6d-467d-80b1-1e3c0c4bee5f",
               "schema_id":"LnXR1rPnncTPZvRdmJKhJQ:2:degree schema:68.21.36"
            },
            "interval":"None"
         }
      ]
   }
}
```

* **Result for Protocol Type 4.0**
```
{
   "attributes":{
      "attribute_0":{
         "credentials":[
            {
               "requested_attributes":{ // requested attributes
                  "MemberID":"435345"
               },
               "cred_info":{
                  "attrs":{
                     "Age":"27",
                     "FirstName":"Rebecca",
                     "Lastname":"Greaves",
                     "MemberID":"435345",
                     "Salary":"1000",
                     "Sex":"Male"
                  },
                  "cred_def_id":"LnXR1rPnncTPZvRdmJKhJQ:3:CL:273930:tag",
                  "referent":"67bf1885-5f6d-467d-80b1-1e3c0c4bee5f",
                  "schema_id":"LnXR1rPnncTPZvRdmJKhJQ:2:degree schema:68.21.36"
               },
               "interval":"None"
            }
         ],
         "name":"MemberID", // name of attribute
         "missing":false, // if an attribute is missing
         "self_attest_allowed":true // if an attribute can be self-attested
      }
   },
   "predicates":{
      "predicate_0":{
         "credentials":[
            {
               "requested_attributes":{ // attribute requested in predicate
                  "Age":"27"
               },
               "cred_info":{
                  "attrs":{
                     "Age":"27",
                     "FirstName":"Rebecca",
                     "Lastname":"Greaves",
                     "MemberID":"435345",
                     "Salary":"1000",
                     "Sex":"Male"
                  },
                  "cred_def_id":"LnXR1rPnncTPZvRdmJKhJQ:3:CL:273930:tag",
                  "referent":"67bf1885-5f6d-467d-80b1-1e3c0c4bee5f",
                  "schema_id":"LnXR1rPnncTPZvRdmJKhJQ:2:degree schema:68.21.36"
               },
               "interval":"None"
            }
         ],
         "name":"Age", // name of attribute
         "p_type":"<=", // predicate type
         "p_value":30, // predicate value
         "missing":false // if a predicate is missing
      }
   }
}
```
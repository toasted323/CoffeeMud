CREATE TABLE CMVFS (
	CMFNAM char (255),
	CMDTYP int ,
	CMMODD bigint,
	CMWHOM char (50) NULL,
	CMDATA text NULL
);

ALTER TABLE CMVFS
	ADD 
	( 
		PRIMARY KEY (CMFNAM)
	);

CREATE TABLE CMCHAB (
	CMUSERID char (50) NULL ,
	CMABID char (50) NULL ,
	CMABPF int NULL ,
	CMABTX text NULL
);

ALTER TABLE CMCHAB
	ADD 
	( 
		PRIMARY KEY (CMUSERID,CMABID)
	);
	
CREATE TABLE CMSTAT (
	CMSTRT bigint NULL ,
	CMENDT bigint NULL ,
	CMDATA text NULL
);

ALTER TABLE CMSTAT
	ADD 
	( 
		PRIMARY KEY (CMSTRT)
	);

CREATE TABLE CMPOLL (
	CMNAME char (100) ,
	CMBYNM char (100) NULL ,
	CMSUBJ char (255) NULL ,
	CMDESC text NULL ,
	CMOPTN text NULL ,
	CMFLAG bigint NULL ,
	CMQUAL char (255) NULL ,
	CMRESL text NULL,
	CMEXPI bigint NULL
);

ALTER TABLE CMPOLL
	ADD 
	( 
		PRIMARY KEY (CMNAME)
	);
	
CREATE TABLE CMCHAR (
    CMCHID char (50),
	CMUSERID char (50) ,
	CMPASS char (50) NULL ,
	CMCLAS char (250) NULL ,
	CMSTRE int NULL ,
	CMRACE char (250) NULL ,
	CMDEXT int NULL ,
	CMCONS int NULL ,
	CMGEND char (50) NULL ,
	CMWISD int NULL ,
	CMINTE int NULL ,
	CMCHAR int NULL ,
	CMHITP int NULL ,
	CMLEVL char (50) NULL ,
	CMMANA int NULL ,
	CMMOVE int NULL ,
	CMDESC text NULL ,
	CMALIG int NULL ,
	CMEXPE int NULL ,
	CMEXLV int NULL ,
	CMWORS char (50) NULL ,
	CMPRAC int NULL ,
	CMTRAI int NULL ,
	CMAGEH int NULL ,
	CMGOLD int NULL ,
	CMWIMP int NULL ,
	CMQUES int NULL ,
	CMROID char (100) NULL ,
	CMDATE char (70) NULL ,
	CMCHAN int NULL ,
	CMATTA int NULL ,
	CMAMOR int NULL ,
	CMDAMG int NULL ,
	CMBTMP int NULL ,
	CMLEIG char (50) NULL ,
	CMHEIT int NULL ,
	CMWEIT int NULL ,
	CMPRPT char (250) NULL,
	CMCOLR char (255) NULL,
	CMLSIP char (100) NULL,
	CMEMAL char (255) NULL,
	CMPFIL text NULL,
	CMSAVE char (150) NULL,
	CMMXML text NULL
);

ALTER TABLE CMCHAR
	ADD 
	( 
		PRIMARY KEY (CMUSERID)
	);

CREATE TABLE CMCHFO (
	CMUSERID char (50) NULL ,
	CMFONM int NULL ,
	CMFOID char (50) NULL ,
	CMFOTX text NULL ,
	CMFOLV int NULL ,
	CMFOAB int NULL 
);

ALTER TABLE CMCHFO
	ADD 
	( 
		PRIMARY KEY (CMUSERID,CMFONM)
	);

CREATE TABLE CMCHCL (
	CMUSERID char (50) NULL ,
	CMCLAN char (100) NULL ,
	CMCLRO int NULL,
	CMCLSTS char (100) NULL
);

ALTER TABLE CMCHCL
	ADD 
	( 
		PRIMARY KEY (CMUSERID,CMCLAN)
	);

CREATE TABLE CMCHIT (
	CMUSERID char (50) NULL ,
	CMITNM char (100) NULL ,
	CMITID char (50) NULL ,
	CMITTX text NULL ,
	CMITLO char (100) NULL ,
	CMITWO bigint NULL ,
	CMITUR int NULL ,
	CMITLV int NULL ,
	CMITAB int NULL ,
	CMHEIT int NULL
);

ALTER TABLE CMCHIT
	ADD 
	( 
		PRIMARY KEY (CMUSERID,CMITNM)
	);

CREATE TABLE CMROCH (
	CMROID char (50) NULL ,
	CMCHNM char (100) NULL ,
	CMCHID char (50) NULL ,
	CMCHTX text NULL ,
	CMCHLV int NULL ,
	CMCHAB int NULL ,
	CMCHRE int NULL ,
	CMCHRI char (100) NULL
);

ALTER TABLE CMROCH 
	ADD 
	( 
		PRIMARY KEY (CMROID,CMCHNM)
	);

CREATE TABLE CMROEX (
	CMROID char (50) NULL ,
	CMDIRE int NULL ,
	CMEXID char (50) NULL ,
	CMEXTX text NULL ,
	CMNRID char (50) NULL 
);

ALTER TABLE CMROEX 
	ADD 
	( 
		PRIMARY KEY (CMROID,CMDIRE)
	);

CREATE TABLE CMROIT (
	CMROID char (50) NULL ,
	CMITNM char (100) NULL ,
	CMITID char (50) NULL ,
	CMITLO char (100) NULL ,
	CMITTX text NULL ,
	CMITRE int NULL ,
	CMITUR int NULL ,
	CMITLV int NULL ,
	CMITAB int NULL ,
	CMHEIT int NULL
);

ALTER TABLE CMROIT 
	ADD 
	( 
		PRIMARY KEY (CMROID,CMITNM)
	);

CREATE TABLE CMROOM (
	CMROID char (50) NULL ,
	CMLOID char (50) NULL ,
	CMAREA char (50) NULL ,
	CMDESC1 char (255) NULL ,
	CMDESC2 text NULL ,
	CMROTX text NULL 
);

ALTER TABLE CMROOM 
	ADD 
	( 
		PRIMARY KEY (CMROID)
	);


CREATE TABLE CMQUESTS (
	CMQUESID char (250) NULL ,
	CMQUTYPE char (50) NULL ,
	CMQFLAGS int NULL ,
	CMQSCRPT text NULL ,
	CMQWINNS text NULL
);

ALTER TABLE CMQUESTS 
	ADD 
	( 
		PRIMARY KEY (CMQUESID)
	);


CREATE TABLE CMAREA (
	CMAREA char (50) ,
	CMTYPE char (50) ,
	CMCLIM int NULL ,
	CMSUBS char (100) NULL ,
	CMDESC text NULL ,
	CMROTX text NULL ,
	CMTECH int NULL
);

ALTER TABLE CMAREA 
	ADD 
	( 
		PRIMARY KEY (CMAREA)
	);

CREATE TABLE CMJRNL (
	CMJKEY char (160) ,
	CMJRNL char (100) NULL ,
	CMFROM char (50) NULL ,
	CMDATE char (50)  NULL ,
	CMTONM char (50) NULL ,
	CMSUBJ char (255) NULL ,
	CMPART char (75) NULL ,
	CMATTR integer NULL,
	CMDATA char (255) NULL ,
	CMUPTM bigint NULL,
	CMIMGP char (50) NULL,
	CMVIEW integer NULL,
	CMREPL integer NULL,
	CMMSGT text NULL,
	CMEXPI bigint NULL
);

ALTER TABLE CMJRNL 
	ADD 
	( 
		PRIMARY KEY (CMJKEY)
	);

CREATE INDEX CMJRNLNAME on CMJRNL (CMJRNL ASC);
CREATE INDEX CMJRNLCMPART on CMJRNL (CMPART ASC);
CREATE INDEX CMJRNLCMTONM on CMJRNL (CMTONM ASC);
CREATE INDEX CMJRNLCMUPTM on CMJRNL (CMUPTM ASC);
CREATE INDEX CMJRNLCMEXPI on CMJRNL (CMEXPI ASC);

CREATE TABLE CMCLAN (
	CMCLID char (100) ,
	CMTYPE int ,
	CMDESC text NULL ,
	CMACPT char (255) NULL ,
	CMPOLI text NULL ,
	CMRCLL char (50) NULL ,
	CMDNAT char (50) NULL ,
	CMSTAT int NULL ,
	CMMORG char (50) NULL ,
	CMTROP int NULL
);

ALTER TABLE CMCLAN 
	ADD 
	( 
		PRIMARY KEY (CMCLID)
	);

CREATE TABLE CMPDAT (
	CMPLID char (100) ,
	CMSECT char (100) ,
	CMPKEY char (255) ,
	CMPDAT text NULL 
);

ALTER TABLE CMPDAT 
	ADD 
	( 
		PRIMARY KEY (CMPLID,CMSECT,CMPKEY)
	);

CREATE TABLE CMGRAC (
	CMRCID char (250) ,
	CMRDAT text NULL , 
	CMRCDT bigint NULL
);

ALTER TABLE CMGRAC 
	ADD 
	( 
		PRIMARY KEY (CMRCID)
	);
	
CREATE TABLE CMCCAC (
	CMCCID char (50) ,
	CMCDAT text NULL 
);

ALTER TABLE CMCCAC 
	ADD 
	( 
		PRIMARY KEY (CMCCID)
	);

CREATE TABLE CMGAAC (
	CMGAID char (50) ,
	CMGAAT text NULL , 
	CMGACL char (50) NULL
);

ALTER TABLE CMGAAC 
	ADD 
	( 
		PRIMARY KEY (CMGAID)
	);

CREATE TABLE CMACCT (
	CMANAM char (50) ,
	CMPASS char (50) ,
	CMCHRS text NULL ,
	CMAXML text  NULL 
);

ALTER TABLE CMACCT 
	ADD 
	( 
		PRIMARY KEY (CMANAM)
	);

CREATE TABLE CMBKLG (
	CMNAME char (50),
	CMINDX int,
	CMSNAM int,
	CMDATE bigint NULL,
	CMDATA text NULL
);

ALTER TABLE CMBKLG
	ADD 
	( 
		PRIMARY KEY (CMNAME,CMINDX)
	);

CREATE TABLE CMCLIT (
	CMCLID char (100) ,
	CMITNM char (100) ,
	CMITID char (50) NULL ,
	CMITTX text NULL ,
	CMITLO char (100) NULL ,
	CMITWO bigint NULL ,
	CMITUR int NULL ,
	CMITLV int NULL ,
	CMITAB int NULL ,
	CMHEIT int NULL
);

ALTER TABLE CMCLIT
	ADD 
	( 
		PRIMARY KEY (CMCLID,CMITNM)
	);


SOURCES= \
	FlashException.java \
	LatexToMeJ.java \
	MeJToLatex.java \
	QuestionComparator.java \
	SC2K.java \
	SmacGUI.java \
	SmacOptionData.java \
	SmacOptionDialog.java \
	SmacParser.java \
	SmacToMeJ.java \
	SmacUI.java

JARS =  SmacParser.jar \
	SmacGUI.jar \
	SC2K.jar

JARS_3RDPARTY = mysql-connector-java-5.1.12-bin.jar


#***************************************************************************
#
#  Section 1: Directories.
#
#***************************************************************************
CLASS_DIR        = ../classes
DOC_DIR          = ../docs
JAR_DIR          = ../jars
SOURCE_DIR       = ./
DESTINATION      = ../classes

JAVA_BIN         = /usr/bin

#***************************************************************************
#
#  Section 2. Tools and options.
#
#***************************************************************************

JAVA     = java
JAVAC    = javac
JAR      = jar
DELETE   = rm -f
COPY     = cp
PROFILER = -Xrunhprof
MAKEDIR  = mkdir -p
PRINT    = @echo
JAVADOC  = javadoc

SEP = :

EMPTY            =
SPACE            = $(EMPTY) $(EMPTY)

LOCAL_JARTMP     = $(patsubst %,$(JAR_DIR)/%,$(JARS))
LOCAL_JARLIST    = $(subst $(SPACE),$(SEP),$(LOCAL_JARTMP))
OTHER_JARLIST    = $(subst $(SPACE),$(SEP),$(JARS_3RDPARTY))

JRE              = /usr/lib/jvm/java-1.6.0-openjdk-1.6.0.0.x86_64/jre/lib/rt.jar

SOURCEPATH       = $(SOURCE_DIR)
CLASSPATH        = $(JRE)$(SEP)$(CLASS_DIR)$(SEP)$(LOCAL_JARLIST)$(SEP)$(OTHER_JARLIST)

JAVAC_OPTIONS    = -d $(DESTINATION) -classpath $(CLASSPATH) -sourcepath $(SOURCEPATH) -Xlint:-path -g
JAVA_OPTIONS     = -ea -classpath $(CLASSPATH) 
JAR_OPTIONS      = -cvmf
PROFILER_OPTIONS = cpu=times,depth=6

JAVADOC_OPTIONS  = \
	-d $(DOC_DIR) \
	-sourcepath $(SOURCE_DIR) \
	-classpath $(CLASSPATH) \
	-link http://java.sun.com/javase/6/docs/api/ \
	-package \
	-use \
	-splitIndex \
	-version \
	-windowtitle $(WINDOWTITLE) \
	-doctitle $(DOCTITLE) \
	-header $(HEADER) \
	-bottom $(BOTTOM)


CLASS_FILES     = $(patsubst %.java,$(CLASS_DIR)/%.class,$(SOURCES))
JAR_FILES       = $(patsubst %,$(JAR_DIR)/%,$(JARS))

SMAC_PARSER_JAR_HELPER=$(subst $$,\$$,$(patsubst ../classes/%,-C ../classes %,$(wildcard ../classes/SmacParser*.class) ../classes/SmacOptionData.class ../classes/SmacUI.class ../classes/QuestionComparator.class))

all:  $(CLASS_FILES) $(JAR_FILES)

clean:
	rm -f $(CLASS_DIR)/* $(JAR_FILES)

# Rule 1. Building a .class file from a .java file
$(CLASS_DIR)/%.class : %.java
	$(JAVA_BIN)/$(JAVAC) $(JAVAC_OPTIONS) $< 

$(JAR_DIR)/SmacGUI.jar: $(CLASS_FILES) smacparser.ini smacparser-legacy.ini latextomej.ini smacgui.ini
	$(JAR) $(JAR_OPTIONS) $(JAR_DIR)/SmacGUI.mf $(JAR_DIR)/SmacGUI.jar smacparser.ini smacparser-legacy.ini latextomej.ini smacgui.ini -C $(CLASS_DIR) .

$(JAR_DIR)/SmacParser.jar: $(CLASS_DIR)/SmacParser.class $(CLASS_DIR)/QuestionComparator.class smacparser.ini smacparser-legacy.ini
	$(JAR) $(JAR_OPTIONS) $(JAR_DIR)/SmacParser.mf $(JAR_DIR)/SmacParser.jar smacparser.ini smacparser-legacy.ini $(SMAC_PARSER_JAR_HELPER)

$(JAR_DIR)/SC2K.jar: $(JAR_DIR)/SmacParser.jar $(CLASS_DIR)/SC2K.class sc2k.ini 
	$(JAR) $(JAR_OPTIONS) $(JAR_DIR)/SC2K.mf $(JAR_DIR)/SC2K.jar sc2k.ini $(CLASS_DIR)/SC2K.class smacparser-legacy.ini $(SMAC_PARSER_JAR_HELPER)

zip: $(JAR_DIR)/SmacParser.jar $(JAR_DIR)/SmacGUI.jar
	zip smac $(CLASS_DIR)/* LatexToMeJ.java latextomej.ini SmacToMeJ.java smactomej.ini SmacParser.java  SmacParser.jar smacparser.ini smacparser-legacy.ini $(JAR_DIR)/* Makefile
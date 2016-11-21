# extargsparse4j 
>  java port for [extargsparse] (https://github.com/jeppeter/extargsparse)

Maven configuration:

    <dependency>
        <groupId>com.github.jeppeter</groupId>
        <artifactId>extargsparse4j</artifactId>
        <version>1.0</version>
    </dependency>

Gradle configuration

```groovy
    compile group: 'com.github.jeppeter', name: 'extargsparse4j', version: '1.0'
```

## Different from python
>  the parse_command_line can not be  null

```java
import com.github.jeppeter.extargsparse4j.Parser;
import com.github.jeppeter.extargsparse4j.NameSpaceEx;

public class ParserTest {
    public static void main(String[] args) {
        Parser parser;
        String commandline = "{"
            + "    \"verbose|v##verbose count##\" : \"+\"\n"
            + "    \"port|p\" : 3000,\n"
            + "    \"$\": \"*\"\n"
            + "}";
        NameSpaceEx ns;
        parser = new Parser();
        parser.load_command_line_string(commandline);
        ns = parser.parse_command_line(args);
        System.out.println(String.format("port %s",ns.getObject("port").toString()));
        System.out.println(String.format("verbose %d",ns.getLong("verbose")));
        System.out.println(String.format("args %s",ns.getObject("args").toString()));
    }
}
```

```shell
    $ java   ParserTest -p 5000 -vvvv hello world
    port 5000
    verbose 4
    args ["hello","world"]
```


## most complex sample

```java
import com.github.jeppeter.extargsparse4j.ParserException;
import com.github.jeppeter.extargsparse4j.Parser;
import com.github.jeppeter.extargsparse4j.NameSpaceEx;
import com.github.jeppeter.extargsparse4j.Environ;
import com.github.jeppeter.extargsparse4j.Priority;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;


public class ParserTest {
    public static void unset_environs(String[] envkey) {
        for (String c : envkey) {
            Environ.unsetenv(c);
        }
        return;
    }

    public static String write_temp_file(String pattern, String suffix, String content) {
        String retfilename = null;
        while (true) {
            retfilename = null;
            try {
                File temp = File.createTempFile(pattern, suffix);
                FileOutputStream outf;
                retfilename = temp.getAbsolutePath();
                outf = new FileOutputStream(retfilename);
                outf.write(content.getBytes());
                outf.close();
                return retfilename;
            } catch (IOException e) {
                ;
            }
        }
    }


    public static void main(String[] cmdargs) throws Exception {
        String commandline="{\n"
            + "    \"verbose|v\" : \"+\",\n"
            + "    \"$port|p\" : {\n"
            + "        \"value\" : 3000,\n"
            + "        \"type\" : \"int\",\n"
            + "        \"nargs\" : 1 , \n"
            + "        \"helpinfo\" : \"port to connect\"\n"
            + "    },\n"
            + "    \"dep\" : {\n"
            + "        \"list|l\" : [],\n"
            + "        \"string|s\" : \"s_var\",\n"
            + "        \"$\" : \"+\"\n"
            + "    }\n"
            + "}";
        Parser parser;
        String depjsonfile = null,jsonfile=null;
        String[] needenvs = {"EXTARGSPARSE_JSON", "DEP_JSON", "EXTARGS_VERBOSE", "EXTARGS_PORT", "DEP_LIST", "DEP_STRING"};
        String[] params = {"-p","9000","dep","--dep-string","ee","ww"};
        String depstrval,deplistval;
        NameSpaceEx args;
        Priority[] priority= {Priority.ENV_COMMAND_JSON_SET,Priority.ENVIRONMENT_SET,Priority.ENV_SUB_COMMAND_JSON_SET};
        int i;
        Object val;
        ParserTest.unset_environs(needenvs);

        try {
            depstrval = "newval";
            deplistval = "[\"depenv1\",\"depenv2\"]";
            jsonfile = ParserTest.write_temp_file("parse", ".json", "{\"dep\":{\"list\" : [\"jsonval1\",\"jsonval2\"],\"string\" : \"jsonstring\"},\"port\":6000,\"verbose\":3}\n");
            depjsonfile = ParserTest.write_temp_file("parse",".json","{\"list\":[\"depjson1\",\"depjson2\"]}\n");
            Environ.setenv("DEP_JSON",depjsonfile);
            Environ.setenv("EXTARGSPARSE_JSON",jsonfile);
            Environ.setenv("DEP_STRING",depstrval);
            Environ.setenv("DEP_LIST",deplistval);

            parser = new Parser(priority);
            parser.load_command_line_string(commandline);
            args = parser.parse_command_line(params);
            System.out.println(String.format("verbose %d",args.getLong("verbose")));
            System.out.println(String.format("port %d",args.getLong("port")));
            System.out.println(String.format("subcommand %s",args.getString("subcommand")));
            System.out.println(String.format("dep_list %s",args.get("dep_list").toString()));
            System.out.println(String.format("dep_string %s",args.getString("dep_string")));
            System.out.println(String.format("subnargs %s",args.get("subnargs").toString()));
        } finally {
            if (depjsonfile != null) {
                File file = new File(depjsonfile);
                file.delete();
                depjsonfile = null;
            }
            if (jsonfile != null) {
                File file = new File(jsonfile);
                file.delete();
                jsonfile = null;
            }
        }
        return;
    }
}
```

```shell
verbose 3
port 9000
subcommand dep
dep_list ["jsonval1","jsonval2"]
dep_string ee
subnargs ["ww"]
```


## Rules

* all key is with value of dict will be flag
 **   like this 'flag|f' : true
     --flag or -f will set the False value for this ,default value is True
 **  like 'list|l' : [] 
     --list or -l will append to the flag value ,default is []

* if value is dict, the key is not start with special char ,it will be the sub command name 
  ** for example 'get' : {
       'connect|c' : 'http://www.google.com',
       'newone|N' : false
  } this will give the sub command with two flag (--get-connect or -c ) and ( --get-newone or -N ) default value is 'http://www.google.com' and False

* if value is dict , the key start with '$' it means the flag description dict 
  ** for example '$verbose|v' : {
    'value' : 0,
    'type' : '+',
    'nargs' : 0,
    'help' : 'verbose increment'
  }   it means --verbose or -v will be increment and default value 0 and need args is 0  help (verbose increment)

* if the value is dict ,the key start with '+' it means add more bundles of flags
  **  for example   '+http' : {
        'port|p' : 3000,
        'visual_mode|V' : false
    } --http-port or -p  and --http-visual-mode or -V will set the flags ,short form it will not affected

* if the subcommand follows <.*> it will call function 
  **  for example   'dep<__main__.dep_handler>' : {
        'list|l' : [],
        'string|s' : 's_var',
        '$' : '+'
    }  the dep_handler will call __main__ it is the main package ,other packages will make the name of it ,and the 
       args is the only one add

* special flag '$' is for args in main command '$' for subnargs in sub command


* special flag --json for parsing args in json file in main command
* special flag '--%s-json'%(args.subcommand) for  subcommand for example
   ** --dep-json dep.json will set the json command for dep sub command ,and it will give the all omit the command
   for example  "dep<__main__.dep_handler>" : {
        "list|l" : [],
        "string|s" : "s_var",
        "$" : "+"
    }  
    in dep.json
    {
        "list" : ["jsonval1","jsonval2"],
        "string" : "jsonstring"
    }

> because we modify the value in the command line ,so the json file value is ignored

*  you can specify the main command line to handle the json for example
   {
     "dep" : {
        "string" : "jsonstring",
        "list" : ["jsonlist1","jsonlist2"]
     },
     "port" : 6000,
     "verbose" : 4
   }

* you can specify the json file by environment value for main file json file the value is
   **EXTARGSPARSE_JSONFILE
      for subcommand json file is
      DEP_JSONFILE  DEP is the subcommand name uppercase

   ** by the environment variable can be set for main command
      EXTARGSPARSE_PORT  is for the main command -p|--port etc
      for sub command is for DEP_LIST for dep command --list


* note the priority of command line is  this can be change or omit by the extargsparse.ExtArgsParse(priority=[])
   **   command input 
   **   subcommand json file input extargsparse.SUB_COMMAND_JSON_SET
   **   command json file input extargsparse.COMMAND_JSON_SET
   **   environment variable input _if the common args not with any _ in the flag dest ,it will start with EXTARGS_  extargsparse.ENVIRONMENT_SET
   **   environment subcommand json file input extargsparse.ENV_SUB_COMMAND_JSON_SET
   **   environment json file input  extargsparse.ENV_COMMAND_JSON_SET
   **   default value input by the load string


* flag option key
   **  flagname the flagname of the value
   **  shortflag flag set for the short
   **  value  the default value of flag
   **  nargs it accept args "*" for any "?" 1 or 0 "+" equal or more than 1 , number is the number
   **  helpinfo for the help information

* flag format description
   **  if the key is flag it must with format like this 
           [$]?flagname|shortflag+prefix##helpinfo##
        $ is flag start character ,it must be the first character
        flagname name of the flag it is required
        shortflag is just after flagname with |,it is optional
        prefix is just after shortflag with + ,it is optional
        helpinfo is just after prefix with ## and end with ## ,it is optional ,and it must be last part

* command format description
  ** if the key is command ,it must with format like this
           cmdname<function>##helpinfo##
        cmdname is the command name
        function is just after cmdname ,it can be the optional ,it will be the call function name ,it include the packagename like '__main__.call_handler'
        helpinfo is just after function ,it between ## ## it is optional



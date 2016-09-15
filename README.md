YAML Configuration
==================
A Library to use YAML as a configuration file format with [apache commons-configuration](https://commons.apache.org/proper/commons-configuration/).

Commons configuration already supports xml and java properties files, this
adds YAML support.  This uses [SnakeYaml](https://bitbucket.org/asomov/snakeyaml) to digest the configuration files.



### Usage

Dependencies:
```
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-configuration2</artifactId>
    <version>2.1</version>
</dependency>
<dependency>
    <groupId>commons-beanutils</groupId>
    <artifactId>commons-beanutils</artifactId>
    <version>1.9.2</version>
</dependency>
<dependency>
    <groupId>org.yaml</groupId>
    <artifactId>snakeyaml</artifactId>
    <version>1.17</version>
</dependency>
```

In code:
```
import org.m410.config.YamlConfiguration;

YamlConfiguration config = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
         .configure(new Parameters().hierarchical().setFileName("src/test/resources/test1.yml"))
         .getConfiguration();
```


### Issues

 - This is not battled tested in any way.
 - If your using a yaml file with multiple configurations divided by `'---'` it 
  will only work on the first configuration.  I have a workaround in the
  YamlEnvConfiguration file, but it's pretty ugly and will be going away
  shortly.
 - If you have a list of elements, with different values, they probably won't get
  placed at the correct index.  As far as I can see, there's no way to insure the
  index is right when writing out to a text file.
 - Pretty safe to say there's other issues that I haven't found yet, this is ALPHA.
 
  
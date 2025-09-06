# AI Mod Generator Plugin - Installation Guide

## Overview

The AI Mod Generator Plugin for MCreator is a comprehensive tool that uses artificial intelligence and web search to generate Minecraft mod elements through natural language prompts. This guide will walk you through the installation process and initial setup.

## System Requirements

### Minimum Requirements
- **MCreator**: Version 2023.4 or later
- **Java**: Version 11 or later (Java 17 recommended)
- **Operating System**: Windows 10+, macOS 10.14+, or Linux (Ubuntu 18.04+)
- **RAM**: 4GB minimum (8GB recommended)
- **Storage**: 500MB free space for plugin and generated assets
- **Internet Connection**: Required for AI services and web search functionality

### Recommended Requirements
- **MCreator**: Latest version (2024.x)
- **Java**: Version 17 or later
- **RAM**: 8GB or more
- **Storage**: 2GB free space
- **Internet**: Stable broadband connection (for optimal AI performance)

## Pre-Installation Setup

### 1. Verify MCreator Installation
Ensure MCreator is properly installed and running:
```bash
# Check MCreator version
# Open MCreator → Help → About MCreator
# Verify version is 2023.4 or later
```

### 2. Java Version Check
Verify your Java installation:
```bash
# Windows Command Prompt / macOS Terminal / Linux Terminal
java -version

# Should show Java 11 or later
# Example output:
# openjdk version "17.0.2" 2022-01-18
```

### 3. Locate MCreator Plugins Directory
Find your MCreator plugins directory:

**Windows:**
```
%USERPROFILE%\.mcreator\plugins\
# Example: C:\Users\YourName\.mcreator\plugins\
```

**macOS:**
```
~/.mcreator/plugins/
# Example: /Users/YourName/.mcreator/plugins/
```

**Linux:**
```
~/.mcreator/plugins/
# Example: /home/yourusername/.mcreator/plugins/
```

## Installation Methods

### Method 1: Direct Download (Recommended)

1. **Download the Plugin**
   - Visit the [Releases page](https://github.com/mcreator/ai-mod-generator-plugin/releases)
   - Download the latest `ai-mod-generator-plugin-x.x.x.jar` file

2. **Install the Plugin**
   - Copy the downloaded JAR file to your MCreator plugins directory
   - The file structure should look like:
     ```
     .mcreator/
     └── plugins/
         └── ai-mod-generator-plugin-1.0.0.jar
     ```

3. **Restart MCreator**
   - Close MCreator completely
   - Restart MCreator
   - The plugin will be automatically loaded

### Method 2: Build from Source

1. **Clone the Repository**
   ```bash
   git clone https://github.com/mcreator/ai-mod-generator-plugin.git
   cd ai-mod-generator-plugin
   ```

2. **Build the Plugin**
   ```bash
   # Windows
   gradlew.bat build

   # macOS/Linux
   ./gradlew build
   ```

3. **Install Built Plugin**
   ```bash
   # Copy the built JAR to plugins directory
   cp build/libs/ai-mod-generator-plugin-1.0.0.jar ~/.mcreator/plugins/
   ```

### Method 3: Development Installation

For developers who want to modify the plugin:

1. **Setup Development Environment**
   ```bash
   git clone https://github.com/mcreator/ai-mod-generator-plugin.git
   cd ai-mod-generator-plugin
   ```

2. **Import into IDE**
   - Open your preferred Java IDE (IntelliJ IDEA, Eclipse, VS Code)
   - Import the project as a Gradle project
   - Wait for dependencies to download

3. **Build and Test**
   ```bash
   ./gradlew build
   ./gradlew test
   ```

## Post-Installation Configuration

### 1. Verify Installation
After restarting MCreator:
1. Open MCreator
2. Go to **Tools** menu
3. Look for **AI Mod Generator** option
4. If present, installation was successful

### 2. Initial Configuration
1. Click **Tools** → **AI Mod Generator**
2. The plugin dialog will open
3. Configure your AI settings (see Configuration section below)

### 3. API Key Setup
The plugin requires API keys for AI services:

1. **OpenAI API Key** (Recommended)
   - Visit [OpenAI API](https://platform.openai.com/api-keys)
   - Create an account and generate an API key
   - In the plugin settings, enter your API key

2. **Alternative AI Providers**
   - The plugin supports multiple AI providers
   - Configure according to your preferred service

## Configuration

### Basic Configuration
1. **Open Plugin Settings**
   - Tools → AI Mod Generator → Settings

2. **Configure AI Provider**
   ```json
   {
     "aiProvider": "openai",
     "apiKey": "your-api-key-here",
     "model": "gpt-4",
     "temperature": 0.7,
     "maxTokens": 2000
   }
   ```

3. **Set Generation Preferences**
   - Enable/disable specific generation types
   - Adjust quality vs speed settings
   - Configure web search options

### Advanced Configuration
1. **Custom Prompts**
   - Modify AI prompts for specific needs
   - Create custom generation templates

2. **Output Directories**
   - Configure where generated assets are saved
   - Set up automatic organization

3. **Performance Tuning**
   - Adjust concurrent generation limits
   - Configure caching settings

## Troubleshooting

### Common Issues

#### Plugin Not Appearing in Menu
**Symptoms:** AI Mod Generator not visible in Tools menu

**Solutions:**
1. Verify MCreator version (must be 2023.4+)
2. Check plugin file is in correct directory
3. Ensure JAR file is not corrupted
4. Restart MCreator completely
5. Check MCreator logs for errors

#### Java Version Errors
**Symptoms:** Plugin fails to load with Java errors

**Solutions:**
1. Update Java to version 11 or later
2. Verify JAVA_HOME environment variable
3. Restart MCreator after Java update

#### API Connection Issues
**Symptoms:** AI generation fails or times out

**Solutions:**
1. Verify internet connection
2. Check API key validity
3. Test with different AI provider
4. Check firewall/proxy settings

#### Generation Failures
**Symptoms:** Generated content is poor quality or fails

**Solutions:**
1. Improve prompt specificity
2. Adjust generation settings
3. Try different AI models
4. Check available API credits

### Getting Help

#### Log Files
MCreator logs can help diagnose issues:
- **Windows:** `%USERPROFILE%\.mcreator\logs\`
- **macOS/Linux:** `~/.mcreator/logs/`

#### Support Channels
1. **GitHub Issues:** [Report bugs and feature requests](https://github.com/mcreator/ai-mod-generator-plugin/issues)
2. **MCreator Forum:** [Community support](https://mcreator.net/forum)
3. **Discord:** [Real-time help](https://discord.gg/mcreator)
4. **Documentation:** [Full documentation](https://github.com/mcreator/ai-mod-generator-plugin/wiki)

## Uninstallation

To remove the plugin:
1. Close MCreator
2. Delete the plugin JAR file from the plugins directory
3. Restart MCreator
4. The plugin will no longer be available

## Updates

### Automatic Updates
The plugin checks for updates automatically and will notify you when new versions are available.

### Manual Updates
1. Download the latest version
2. Replace the old JAR file with the new one
3. Restart MCreator

## Security Considerations

### API Key Security
- Never share your API keys
- Use environment variables for sensitive keys
- Regularly rotate API keys
- Monitor API usage for unusual activity

### Generated Content
- Review all generated content before use
- Test generated code thoroughly
- Be aware of potential copyright issues
- Follow Minecraft's content guidelines

## Performance Optimization

### System Optimization
1. **Increase Java Heap Size**
   ```bash
   # Add to MCreator launch options
   -Xmx4G -Xms2G
   ```

2. **SSD Storage**
   - Install on SSD for faster asset generation
   - Use SSD for workspace directory

3. **Network Optimization**
   - Use stable internet connection
   - Consider local AI models for offline use

### Plugin Optimization
1. **Batch Generation**
   - Generate multiple items at once
   - Use theme-based generation

2. **Caching**
   - Enable result caching
   - Reuse similar generations

3. **Quality Settings**
   - Balance quality vs speed
   - Use appropriate AI models

## Next Steps

After successful installation:
1. Read the [User Guide](USER_GUIDE.md)
2. Try the [Quick Start Tutorial](QUICK_START.md)
3. Explore [Example Projects](examples/)
4. Join the [Community](https://discord.gg/mcreator)

## Conclusion

The AI Mod Generator Plugin transforms the Minecraft modding experience by leveraging artificial intelligence to create professional-quality mod elements from simple text descriptions. With proper installation and configuration, you'll be creating amazing mods in minutes instead of hours.

For additional support and resources, visit our [GitHub repository](https://github.com/mcreator/ai-mod-generator-plugin) and join the MCreator community.

---

**Version:** 1.0.0  
**Last Updated:** December 2024  
**Compatibility:** MCreator 2023.4+


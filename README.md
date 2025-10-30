# AI Mod Generator Plugin for MCreator

A comprehensive MCreator plugin that uses AI and web search to generate Minecraft mod elements through natural language prompts. Create professional mods with items, blocks, recipes, textures, sounds, and more using simple text descriptions.

## Features

### 🤖 AI-Powered Generation
- **Natural Language Processing**: Describe what you want in plain English
- **Intelligent Analysis**: AI understands context, themes, and mod requirements
- **Balanced Content**: Automatically ensures game balance and compatibility

### 🎨 Complete Asset Creation
- **Items & Blocks**: Generate weapons, tools, armor, decorative blocks, ores, and more
- **Textures**: AI-generated pixel art textures matching your theme
- **Sounds**: Custom sound effects and ambient audio
- **Recipes**: Balanced crafting, smelting, and other recipes
- **Enchantments**: Custom enchantments with unique effects
- **Procedures**: Complex game logic and behaviors

### 🔍 Web-Enhanced Intelligence
- **Research Integration**: Searches the web for Minecraft modding references
- **Community Knowledge**: Learns from existing mods and best practices
- **Up-to-date Information**: Always current with latest Minecraft versions

### 🎯 User-Friendly Interface
- **Clean UI**: Intuitive interface suitable for beginners and experts
- **Progress Tracking**: Real-time generation progress and feedback
- **Batch Generation**: Create multiple related elements at once
- **Theme Consistency**: Maintains visual and gameplay coherence

### ⚙️ Technical Excellence
- **Forge 1.20.1**: Full support with future Forge compatibility
- **MCreator Integration**: Seamless workflow with existing projects
- **Optimized Assets**: Properly formatted textures and sounds
- **Error Handling**: Robust error recovery and user guidance

## Installation

### Prerequisites
- MCreator 2023.4 or later
- Java 17 or later
- Internet connection for AI and web search features

### Quick Install
1. Download the latest release from the [Releases page](https://github.com/mcreator/ai-mod-generator-plugin/releases)
2. Extract the ZIP file
3. Copy the plugin files to your MCreator plugins directory:
   - **Windows**: `%USERPROFILE%/.mcreator/plugins/`
   - **macOS**: `~/.mcreator/plugins/`
   - **Linux**: `~/.mcreator/plugins/`
4. Restart MCreator
5. Find "AI Mod Generator" in the Tools menu

### Build from Source
```bash
git clone https://github.com/mcreator/ai-mod-generator-plugin.git
cd ai-mod-generator-plugin
./gradlew preparePlugin
```

## Quick Start

### Basic Usage
1. Open MCreator and create or open a workspace
2. Go to **Tools** → **AI Mod Generator**
3. Enter a description like: "Create a magical sword that shoots fireballs"
4. Configure generation options
5. Click **Generate** and watch the magic happen!

### Example Prompts
- `"Create a set of emerald tools that are faster than diamond"`
- `"Make a glowing mushroom block that provides light and grows in caves"`
- `"Design a fire-themed armor set with flame resistance"`
- `"Build a crafting table that can create infinite weapons"`

### Advanced Features
- **Theme Mode**: Generate entire themed mod packs
- **Batch Generation**: Create multiple related items at once
- **Custom Styles**: Specify art styles and color schemes
- **Balance Control**: Adjust power levels and rarity

## Configuration

### AI Settings
The plugin supports multiple AI providers. Configure in the settings panel:

```json
{
  "aiProvider": "openai",
  "apiKey": "your-api-key-here",
  "model": "gpt-4",
  "temperature": 0.7,
  "maxTokens": 2000
}
```

### Generation Options
- **Balanced Stats**: Ensures game balance
- **Generate Textures**: Creates custom pixel art
- **Generate Sounds**: Adds audio effects
- **Generate Recipes**: Creates crafting recipes
- **Generate Lore**: Adds item descriptions
- **Web Search**: Uses internet research

## Examples

### Creating a Magic Mod
```
Prompt: "Create a mystical magic mod with crystal-based items"

Generated:
- Crystal Sword (with glowing texture)
- Crystal Pickaxe (faster mining)
- Crystal Block (light-emitting)
- Crystal Ore (rare spawn)
- Enchantment: Crystal Power
- Ambient crystal sounds
- Crafting recipes using crystals
```

### Building a Tech Mod
```
Prompt: "Design industrial machinery and advanced tools"

Generated:
- Steel Ingots and tools
- Industrial Furnace block
- Conveyor Belt system
- Power Generator
- Mechanical sounds
- Complex crafting chains
```

## API Reference

### Core Classes
- `AIModGeneratorCore`: Main generation logic
- `AIIntegrationService`: AI provider interface
- `TextureGenerator`: Image generation
- `SoundGenerator`: Audio generation
- `ItemGenerator`: Item creation
- `BlockGenerator`: Block creation

### Extension Points
```java
// Custom generator example
public class CustomGenerator extends BaseGenerator {
    public String generateCustomElement(String prompt, GenerationOptions options) {
        // Your custom logic here
    }
}
```

## Troubleshooting

### Common Issues

**Plugin not appearing in menu**
- Ensure MCreator version is 2023.4+
- Check plugin files are in correct directory
- Restart MCreator completely

**AI generation fails**
- Verify internet connection
- Check API key configuration
- Try simpler prompts first

**Textures not generating**
- Ensure sufficient disk space
- Check workspace permissions
- Verify image generation settings

**Sounds not working**
- Check audio codec support
- Verify sound file paths
- Test with simple sound effects

### Getting Help
- [Documentation](https://github.com/mcreator/ai-mod-generator-plugin/wiki)
- [Issue Tracker](https://github.com/mcreator/ai-mod-generator-plugin/issues)
- [Community Forum](https://mcreator.net/forum)
- [Discord Server](https://discord.gg/mcreator)

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Development Setup
```bash
git clone https://github.com/mcreator/ai-mod-generator-plugin.git
cd ai-mod-generator-plugin
./gradlew build
```

### Running Tests
```bash
./gradlew test
```

### Code Style
- Follow Java conventions
- Use meaningful variable names
- Add Javadoc comments
- Include unit tests

## Roadmap

### Version 1.1
- [ ] Fabric 1.20.1 support
- [ ] Custom model generation
- [ ] Advanced procedure templates
- [ ] Multi-language support

### Version 1.2
- [ ] Entity generation
- [ ] Dimension creation
- [ ] Biome generation
- [ ] Structure generation

### Version 2.0
- [ ] Visual scripting interface
- [ ] Collaborative features
- [ ] Mod marketplace integration
- [ ] Advanced AI models

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- MCreator team for the excellent modding platform
- OpenAI for AI capabilities
- Minecraft modding community for inspiration
- Beta testers and contributors

## Support

If you find this plugin helpful, consider:
- ⭐ Starring the repository
- 🐛 Reporting bugs and issues
- 💡 Suggesting new features
- 🤝 Contributing code or documentation
- 💬 Sharing with the community

---

**Made with ❤️ for the Minecraft modding community**

*Transform your ideas into amazing Minecraft mods with the power of AI!*


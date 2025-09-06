# AI Mod Generator Plugin - User Guide

## Table of Contents
1. [Getting Started](#getting-started)
2. [Basic Usage](#basic-usage)
3. [Advanced Features](#advanced-features)
4. [Generation Types](#generation-types)
5. [Best Practices](#best-practices)
6. [Troubleshooting](#troubleshooting)
7. [Examples](#examples)

## Getting Started

### First Launch
After installing the plugin, access it through:
1. Open MCreator
2. Create or open a workspace
3. Go to **Tools** → **AI Mod Generator**
4. The plugin interface will open

### Interface Overview
The AI Mod Generator interface consists of several key components:

#### Main Sections
- **Prompt Input Panel**: Where you describe what you want to create
- **Generation Options**: Configure what types of content to generate
- **Output Display**: Shows generation progress and results
- **Settings Panel**: Configure AI providers and preferences

#### Key Controls
- **Generate Button**: Start the generation process
- **Clear Button**: Clear current input and results
- **Save Button**: Save generated content to your mod
- **Settings Button**: Open configuration panel

## Basic Usage

### Simple Item Generation
The most basic use case is generating a single item:

1. **Enter a Prompt**
   ```
   Create a magical sword that glows blue and deals extra damage to undead
   ```

2. **Select Generation Options**
   - ✅ Items
   - ✅ Textures
   - ✅ Recipes
   - ❌ Blocks (not needed)

3. **Click Generate**
   - The AI will analyze your prompt
   - Generate the item with appropriate stats
   - Create a blue glowing texture
   - Design a balanced crafting recipe

4. **Review Results**
   - Check the generated item properties
   - Examine the texture quality
   - Verify the recipe balance

5. **Save to Mod**
   - Click "Save All" to add to your workspace
   - The item will appear in MCreator's element list

### Block Generation
Creating blocks follows a similar process:

1. **Prompt Example**
   ```
   Make a crystal ore block that spawns in caves and glows softly
   ```

2. **Generation Options**
   - ✅ Blocks
   - ✅ Textures
   - ✅ Recipes (for smelting)
   - ✅ Sounds (mining sounds)

3. **Expected Results**
   - Block with appropriate hardness
   - Glowing crystal texture
   - Smelting recipe for crystal items
   - Custom mining sounds

### Theme-Based Generation
Generate multiple related elements with consistent theming:

1. **Theme Prompt**
   ```
   Create a complete fire-themed mod with weapons, armor, blocks, and decorations
   ```

2. **Batch Generation**
   - The AI will create multiple related items
   - Consistent fire theme across all elements
   - Balanced progression system
   - Cohesive visual style

## Advanced Features

### Web Search Integration
The plugin can search the web for inspiration and references:

1. **Enable Web Search**
   - Check "Use Web Search" in options
   - The AI will research your topic online

2. **Benefits**
   - More accurate and detailed results
   - Real-world inspiration
   - Current trends and styles
   - Technical accuracy

3. **Example with Web Search**
   ```
   Create a realistic medieval castle gate block
   ```
   - AI searches for medieval architecture
   - Incorporates authentic design elements
   - Creates historically accurate textures

### Blockbench Model Ideas
Generate 3D model concepts and mockups:

1. **Enable Model Generation**
   - Check "Blockbench Model Ideas"
   - Specify entity, item, or block models

2. **Generated Content**
   - Multiple view mockups (front, side, perspective)
   - Texture templates
   - Modeling instructions
   - Blockbench project templates

3. **Example Usage**
   ```
   Design a dragon entity model with detailed wings and scales
   ```
   - Creates dragon mockup images
   - Generates scale texture patterns
   - Provides modeling guidelines
   - Includes animation suggestions

### Custom AI Prompts
Advanced users can craft detailed prompts for precise control:

#### Detailed Item Prompt
```
Create a legendary two-handed sword called "Flamebringer" with the following specifications:
- Base damage: 12 (higher than diamond sword)
- Fire aspect enchantment built-in
- Durability: 2500 uses
- Texture: Dark steel blade with orange flame patterns
- Crafting: Requires nether star, blaze rods, and diamond sword
- Special ability: Sets enemies on fire for 5 seconds
- Rarity: Epic (purple name)
```

#### Complex Block Prompt
```
Design an "Arcane Workbench" block that:
- Functions as an enhanced crafting table
- Has animated magical particles
- Glows with purple light (light level 10)
- Requires magical materials to craft
- Has a custom GUI with 4x4 crafting grid
- Makes magical sound effects when used
- Texture: Dark wood with glowing runes
```

### Batch Processing
Generate multiple related elements efficiently:

1. **List-Based Generation**
   ```
   Create these magical gems: Ruby, Sapphire, Emerald, Topaz
   Each should have: ore block, refined gem item, tools, and armor
   ```

2. **Progressive Generation**
   ```
   Make a complete tech mod progression:
   Tier 1: Copper tools and basic machinery
   Tier 2: Steel tools and advanced machines  
   Tier 3: Titanium tools and quantum devices
   ```

## Generation Types

### Items
The plugin can generate various item types:

#### Weapons
- **Swords**: Various materials and enchantments
- **Bows**: Different power levels and special arrows
- **Crossbows**: Rapid-fire and explosive variants
- **Tridents**: Aquatic and lightning-based
- **Custom Weapons**: Unique mechanics and abilities

#### Tools
- **Pickaxes**: Mining speed and special abilities
- **Axes**: Tree cutting and combat variants
- **Shovels**: Terrain modification tools
- **Hoes**: Farming and plant growth tools
- **Multi-tools**: Combined functionality

#### Armor
- **Helmets**: Protection and special vision
- **Chestplates**: Defense and movement abilities
- **Leggings**: Speed and jumping enhancements
- **Boots**: Walking effects and protection
- **Full Sets**: Coordinated bonuses

#### Consumables
- **Food**: Hunger restoration and effects
- **Potions**: Custom brewing recipes
- **Scrolls**: One-time use abilities
- **Fuel**: Custom burning materials

### Blocks
Comprehensive block generation capabilities:

#### Decorative Blocks
- **Building Materials**: Stone, wood, metal variants
- **Furniture**: Chairs, tables, decorative items
- **Lighting**: Torches, lamps, glowing blocks
- **Plants**: Flowers, trees, crops

#### Functional Blocks
- **Machines**: Automated processing
- **Storage**: Chests and containers
- **Redstone**: Logic and automation
- **Transportation**: Rails and portals

#### Ores and Resources
- **Ore Blocks**: Custom mining materials
- **Gem Blocks**: Decorative and functional
- **Crystal Formations**: Magical materials
- **Rare Earth**: Advanced technology materials

### Recipes
Intelligent recipe generation:

#### Crafting Recipes
- **Shaped Recipes**: Specific pattern requirements
- **Shapeless Recipes**: Flexible ingredient mixing
- **Progressive Recipes**: Tiered crafting systems
- **Custom Recipes**: Unique crafting mechanics

#### Processing Recipes
- **Smelting**: Furnace-based processing
- **Brewing**: Potion creation
- **Smithing**: Upgrade systems
- **Custom Processing**: Modded machine recipes

### Textures
AI-powered texture creation:

#### Texture Types
- **16x16 Items**: Standard Minecraft resolution
- **16x16 Blocks**: Tileable block textures
- **64x64 Entities**: High-resolution creature skins
- **Custom Sizes**: Specific resolution requirements

#### Texture Styles
- **Minecraft Style**: Vanilla-compatible appearance
- **Realistic**: Detailed and lifelike
- **Cartoon**: Stylized and colorful
- **Dark/Gothic**: Atmospheric and moody
- **Bright/Colorful**: Vibrant and cheerful

#### Special Features
- **Transparency**: Proper alpha channels
- **Animation**: Frame-based animations
- **Glow Effects**: Emissive textures
- **Weathering**: Aged and worn appearances

### Sounds
Custom audio generation:

#### Sound Types
- **Item Sounds**: Use, equip, break effects
- **Block Sounds**: Place, break, step sounds
- **Ambient**: Background atmospheric audio
- **Music**: Custom background tracks

#### Audio Characteristics
- **Duration**: Short effects to long ambient tracks
- **Style**: Realistic, synthetic, or stylized
- **Mood**: Happy, scary, neutral, epic
- **Quality**: Optimized for Minecraft

## Best Practices

### Prompt Writing
Effective prompts lead to better results:

#### Be Specific
❌ **Poor**: "Make a sword"
✅ **Good**: "Create a steel longsword with 8 attack damage and 1500 durability"

#### Include Context
❌ **Poor**: "Blue armor"
✅ **Good**: "Sapphire armor set for underwater exploration with water breathing effects"

#### Specify Balance
❌ **Poor**: "Powerful weapon"
✅ **Good**: "Balanced endgame sword slightly stronger than netherite but with higher crafting cost"

#### Describe Visuals
❌ **Poor**: "Magic item"
✅ **Good**: "Glowing staff with purple crystal tip and golden handle with runic engravings"

### Generation Strategy

#### Start Simple
1. Begin with single items
2. Test generation quality
3. Refine prompts based on results
4. Scale up to complex projects

#### Use Themes
1. Establish consistent themes
2. Generate related elements together
3. Maintain visual and gameplay coherence
4. Create progression systems

#### Iterate and Refine
1. Generate initial versions
2. Review and identify improvements
3. Regenerate with refined prompts
4. Test in-game functionality

### Quality Control

#### Review Generated Content
- **Stats Balance**: Ensure appropriate power levels
- **Texture Quality**: Check for visual consistency
- **Recipe Logic**: Verify crafting makes sense
- **Sound Appropriateness**: Test audio quality

#### Test in Game
- **Functionality**: Verify everything works
- **Balance**: Check gameplay impact
- **Compatibility**: Test with other mods
- **Performance**: Monitor resource usage

#### Backup and Version Control
- **Save Originals**: Keep generated files
- **Document Changes**: Track modifications
- **Version Control**: Use Git for projects
- **Share Results**: Contribute to community

## Troubleshooting

### Common Issues

#### Poor Generation Quality
**Symptoms**: Generated content doesn't match expectations

**Solutions**:
1. Improve prompt specificity
2. Add more descriptive details
3. Use reference examples
4. Adjust AI temperature settings

#### Unbalanced Items
**Symptoms**: Items too powerful or too weak

**Solutions**:
1. Enable "Balanced Stats" option
2. Specify power level in prompt
3. Compare to vanilla items
4. Use progressive difficulty

#### Texture Issues
**Symptoms**: Textures have wrong colors or style

**Solutions**:
1. Specify exact colors in prompt
2. Reference existing Minecraft textures
3. Use "no glow" instruction for standard textures
4. Enable background removal

#### Recipe Problems
**Symptoms**: Recipes too expensive or too cheap

**Solutions**:
1. Specify ingredient preferences
2. Reference vanilla recipe costs
3. Consider item power level
4. Test recipe accessibility

### Performance Issues

#### Slow Generation
**Causes**: Large requests, slow internet, API limits

**Solutions**:
1. Break large requests into smaller parts
2. Check internet connection
3. Verify API key and limits
4. Use faster AI models

#### Memory Usage
**Causes**: Large texture generation, many simultaneous requests

**Solutions**:
1. Increase Java heap size
2. Generate fewer items at once
3. Clear cache regularly
4. Close other applications

## Examples

### Example 1: Magic Mod
**Goal**: Create a complete magic-themed mod

**Prompt**:
```
Create a comprehensive magic mod with the following elements:
- Magical crystals (red, blue, green) as base materials
- Crystal ores that spawn in different biomes
- Magic wands for each crystal type with unique spells
- Enchanted robes that boost magical abilities
- Magical workbench for crafting spells
- Potion ingredients from magical plants
- Ambient magical sounds and particle effects
```

**Expected Results**:
- 3 crystal ore blocks with appropriate textures
- 3 refined crystal items
- 3 magic wands with different abilities
- Magic robe armor set
- Enchanted workbench block
- Various magical plants and ingredients
- Custom sounds and effects

### Example 2: Tech Mod
**Goal**: Industrial technology mod

**Prompt**:
```
Design an industrial technology mod featuring:
- Copper, tin, and steel as new materials
- Ore processing machines (crusher, smelter, assembler)
- Advanced tools with durability and efficiency bonuses
- Power generation (steam engines, solar panels)
- Automated systems (conveyor belts, robotic arms)
- Industrial building blocks (reinforced concrete, steel beams)
- Mechanical sounds and steam effects
```

**Expected Results**:
- New metal ores and ingots
- Industrial processing machines
- Advanced tool sets
- Power generation blocks
- Automation components
- Industrial building materials
- Mechanical sound effects

### Example 3: Adventure Mod
**Goal**: RPG-style adventure content

**Prompt**:
```
Create an RPG adventure mod with:
- Legendary weapons with unique abilities
- Ancient armor sets with set bonuses
- Dungeon blocks (mossy stone, iron bars, treasure chests)
- Adventure items (rope, grappling hook, lantern)
- Monster drops and rare materials
- Quest items and collectibles
- Epic music and atmospheric sounds
```

**Expected Results**:
- Legendary weapon collection
- Themed armor sets with bonuses
- Dungeon building materials
- Adventure utility items
- Rare crafting materials
- Quest-related items
- Adventure-themed audio

## Conclusion

The AI Mod Generator Plugin revolutionizes Minecraft modding by making professional-quality content creation accessible to everyone. Whether you're a beginner looking to create your first mod or an experienced developer seeking to accelerate your workflow, this plugin provides the tools and intelligence needed to bring your ideas to life.

Key takeaways:
- Start with clear, specific prompts
- Use the web search feature for better results
- Generate related elements together for consistency
- Always test generated content in-game
- Iterate and refine based on results

With practice and experimentation, you'll be creating amazing Minecraft mods that rival professionally developed content. The only limit is your imagination!

For additional resources, examples, and community support, visit our [GitHub repository](https://github.com/mcreator/ai-mod-generator-plugin) and join the MCreator community.

---

**Version:** 1.0.0  
**Last Updated:** December 2024  
**Plugin Compatibility:** MCreator 2023.4+


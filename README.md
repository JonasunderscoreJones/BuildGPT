This mod adds a `/buildgpt` command that allows the player to place AI generated structures into the world with a customizable prompt.

# Setup
An OpenAI Key is required for this mod in order to work.

create a file `.openaikey` in the config folder or run the `/buildbpt` command with valid parameters once to generate the file and then place your API Key in there.

The command should now work!

# Command
The command has 2 modes:
### Bound
`/buildgpt bound <x> <y> <z> <x> <y> <z> <prompt>`
This mode allows the user to set a bounding box outside of which the mod won't place any blocks.

The prompt is directing ChatGPT to generate a structure within those bounds but the mod also has a check after the response, that filters out any blocks outside the bounding box.

### Unbound
`/buildgpt unbound <x> <y> <z> <prompt>`
This mode only has a starting point from which the AI will (in most cases) generate the structure towards increasing coordinates.

Of course this is not always the case as ChatGPT can sometimes return funny responses with unexpected behaviour, which is why the `bound` mode exists and the `unbound` mode should by used with caution, since placed blocks cannot be undone. During testing, I noticed that structures will **usually** not exceed a 20x20x20 block cube unless prompted to do otherwhise.

# Prompt
There is a predefined prompt in the Mod, meaning that the user is only required to input the building name or description into the `<prompt>` field of the command, since the mod requires the response to be formatted a certain way.

The prompt can also be overwritten by inputting a custom prompt into the `<prompt>` field of the command.

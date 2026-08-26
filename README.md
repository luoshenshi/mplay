# MPlay ported to Minecraft 26.2

Enjoy music with your friends. This mod allows you to stream **Spotify**, **YouTube**, **SoundCloud**, **BandCamp**, **Vimeo**, **Twitch**, *mp3*, *flac*, *wav*, *m3u*, and more into SimpleVoiceChat groups.
Powered by the lightweight [lavaplayer](https://github.com/lavalink-devs/lavaplayer) library.

https://github.com/ItzDerock/simplevoicechat-music/assets/14848722/c974d7a7-26a7-44b9-9c8a-b6d6722a8582

## Commands

- `/music play <song>` - Searches and queues the first result
- `/music search <song>` - Lists all results and lets you choose which you want to queue
- `/music now-playing` - Shows the current song
- `/music queue` - Shows the queue
- `/music skip` - Skips the current song
- `/music pause` - Pauses the current song
- `/music resume` - Resumes the current song
- `/music stop` - Stops the current song and clears the queue
- `/music volume <int;1-100>` - Sets the volume
- `/music kill` - use when something goes wrong, and you want to restart the plugin without restarting the server
- `/music bassboost <float;0-200>` - adds bass boost
- `/music-gui` - opens GUI (Client-Only)

Song can be a SoundCloud URL, YouTube URL, BandCamp URL, Spotify URL, etc. Or it can be just a search term.
By default, it will search on YouTube. You can force it to search on SoundCloud by using the query
`"scsearch: your search terms"`.
Lavaplayer also supports YouTube. To search YouTube Music, use the query `"ytmsearch: your search terms"`.

> **_NOTE ⚠️:_**\
> To play Spotify URLs, add the `spotify:` prefix in your command.  
> Example: `/music play "spotify: ..."`

## Support

This is a fork of the original project, and I’m not actively providing support for it right now.

You can still open an issue if you run into bugs or have questions, but responses and fixes are not guaranteed.

## Customization

This project was originally made for private use, so customization options are limited.

Feel free to fork it further.

## Contributors

<a href="https://github.com/techarecrazy">
  <img src="https://github.com/techarecrazy.png" width="60px;" alt="techarecrazy"/>
</a>

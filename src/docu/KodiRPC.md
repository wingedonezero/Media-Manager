
some KodiRPC examples for localhost  

[get Version](http://127.0.0.1:8080/jsonrpc?request={"jsonrpc":"2.0","id":"1","method":"Application.GetProperties","params":{"properties":["version"]}})

[get datasources](http://127.0.0.1:8080/jsonrpc?request={"jsonrpc":"2.0","id":"1","method":"Files.GetSources","params":{"media":"video"}})

[get Movies](http://127.0.0.1:8080/jsonrpc?request={"jsonrpc":"2.0","id":"1","method":"VideoLibrary.GetMovies","params":{"properties":["file"]}})

[get TvShows](http://127.0.0.1:8080/jsonrpc?request={"jsonrpc":"2.0","id":"1","method":"VideoLibrary.GetTVShows","params":{"properties":["file"]}})

[get episodes for TvShowID](http://127.0.0.1:8080/jsonrpc?request={"jsonrpc":"2.0","id":"1","method":"VideoLibrary.GetEpisodes","params":{"tvshowid":11,"properties":["file"]}})
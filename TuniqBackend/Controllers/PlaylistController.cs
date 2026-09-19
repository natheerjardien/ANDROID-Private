using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using TuniqBackend.Data;
using TuniqBackend.Models;

namespace TuniqBackend.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class PlaylistsController : ControllerBase
    {
        private readonly TuniqDbContext _context;

        public PlaylistsController(TuniqDbContext context)
        {
            _context = context;
        }

        // Route parameters allow for targeted data retrieval, such as fetching playlists by UserId (Microsoft, 2026).
        [HttpGet("user/{userId}")]
        public async Task<ActionResult<IEnumerable<Playlist>>> GetUserPlaylists(string userId)
        {
            var playlists = await _context.Playlists
                .Where(p => p.UserId == userId)
                .Include(p => p.Songs) // loading includes related song entities in the query payload
                .ToListAsync();

            if (!playlists.Any())
            {
                return NotFound();
            }

            return playlists;
        }

        [HttpPost]
        public async Task<ActionResult<Playlist>> CreatePlaylist(Playlist playlist)
        {
            _context.Playlists.Add(playlist);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetUserPlaylists), new { userId = playlist.UserId }, playlist);
        }
    }
}

/* Reference List:

 * Microsoft, 2026. Routing to controller actions in ASP.NET Core. [Online] Available at: < https://learn.microsoft.com/en-us/aspnet/core/mvc/controllers/routing > [Accessed 17 September 2026].

*/
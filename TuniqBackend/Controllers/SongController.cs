using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using TuniqBackend.Data;
using TuniqBackend.Models;

namespace TuniqBackend.Controllers
{
    // The [ApiController] attribute enables API-specific behaviours like automatic model validation responses (Microsoft, 2026a).
    [Route("api/[controller]")]
    [ApiController]
    public class SongsController : ControllerBase
    {
        private readonly TuniqDbContext _context;

        public SongsController(TuniqDbContext context)
        {
            _context = context;
        }

        [HttpGet]
        public async Task<ActionResult<IEnumerable<Song>>> GetSongs()
        {
            // Asynchronous execution prevents thread blocking during database I/O operations (Microsoft, 2026b).
            return await _context.Songs.ToListAsync();
        }

        [HttpPost]
        public async Task<ActionResult<Song>> PostSong(Song song)
        {
            _context.Songs.Add(song);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetSongs), new { id = song.TrackId }, song);
        }
    }
}

/* Reference List:

 * Microsoft, 2026a. Create web APIs with ASP.NET Core. [Online] Available at: < https://learn.microsoft.com/en-us/aspnet/core/web-api/ > [Accessed 17 September 2026].

 * Microsoft, 2026b. Asynchronous programming with async and await. [Online] Available at: < https://learn.microsoft.com/en-us/dotnet/csharp/asynchronous-programming/ > [Accessed 17 September 2026].
 
*/
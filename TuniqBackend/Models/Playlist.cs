using System.ComponentModel.DataAnnotations;

namespace TuniqBackend.Models
{
    // Navigation properties (like the List<Song>) allow EF Core to automatically manage relational database foreign keys (Microsoft, 2026).
    public class Playlist
    {
        [Key]
        public string PlaylistId { get; set; } = Guid.NewGuid().ToString();

        [Required]
        public string Title { get; set; } = string.Empty;

        public string UserId { get; set; } = string.Empty;

        public string Description { get; set; } = string.Empty;

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        public List<Song> Songs { get; set; } = new List<Song>();
    }
}

/* Reference List:

 * Microsoft, 2026. Relationships - EF Core. [Online] Available at: < https://learn.microsoft.com/en-us/ef/core/modeling/relationships > [Accessed 17 September 2026].

*/
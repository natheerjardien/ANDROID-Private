using System.ComponentModel.DataAnnotations;

namespace TuniqBackend.Models
{
    // Entity Framework Core uses DataAnnotations like [Key] and [Required] to enforce database schema constraints (Microsoft, 2026).
    public class Song
    {
        [Key]
        public string TrackId { get; set; } = string.Empty;

        [Required]
        public string Title { get; set; } = string.Empty;

        public string ArtistName { get; set; } = string.Empty;

        public string AlbumName { get; set; } = string.Empty;

        public string CoverArtUrl { get; set; } = string.Empty;

        public bool IsLiked { get; set; } = false;
    }
}

/* Reference List:

 * Microsoft, 2026. System.ComponentModel.DataAnnotations Namespace. [Online] Available at: < https://learn.microsoft.com/en-us/dotnet/api/system.componentmodel.dataannotations > [Accessed 17 September 2026].

*/
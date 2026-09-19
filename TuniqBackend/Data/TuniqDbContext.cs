using Microsoft.EntityFrameworkCore;
using TuniqBackend.Models;

namespace TuniqBackend.Data
{
    // TuniqDbContext inherits from DbContext to provide querying and saving functionality for the mapped entities (Microsoft, 2026).
    public class TuniqDbContext : DbContext
    {
        public TuniqDbContext(DbContextOptions<TuniqDbContext> options) : base(options) { }

        public DbSet<Song> Songs { get; set; }
        public DbSet<Playlist> Playlists { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // Configuring the many-to-many relationship mapping between Playlists and Songs
            modelBuilder.Entity<Playlist>()
                .HasMany(p => p.Songs)
                .WithMany();
        }
    }
}

/* Reference List:
 
 * Microsoft, 2026. DbContext Class (Microsoft.EntityFrameworkCore). [Online] Available at: < https://learn.microsoft.com/en-us/dotnet/api/microsoft.entityframeworkcore.dbcontext > [Accessed 17 September 2026].

*/
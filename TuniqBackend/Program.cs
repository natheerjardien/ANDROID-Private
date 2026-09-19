
using Microsoft.EntityFrameworkCore;
using TuniqBackend.Data;

namespace TuniqBackend
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            // Add services to the container.
            builder.Services.AddControllers();

            // Registers the EF Core DbContext with the Azure SQL connection string (Microsoft, 2026).
            builder.Services.AddDbContext<TuniqDbContext>(options =>
                options.UseSqlServer(
                    builder.Configuration.GetConnectionString("DefaultConnection"),
                    sqlServerOptionsAction: sqlOptions =>
                    {
                        sqlOptions.EnableRetryOnFailure(
                            maxRetryCount: 5,
                            maxRetryDelay: TimeSpan.FromSeconds(30),
                            errorNumbersToAdd: null);
                    }));

            builder.Services.AddEndpointsApiExplorer();
            builder.Services.AddSwaggerGen();

            var app = builder.Build();

            // Configure the HTTP request pipeline.
            if (app.Environment.IsDevelopment())
            {
                app.UseSwagger();
                app.UseSwaggerUI();
            }

            app.UseHttpsRedirection();

            app.UseAuthorization();

            app.MapControllers();

            app.Run();
        }
    }
}

/* Reference List:
 
 * Microsoft, 2026d. Dependency injection in ASP.NET Core. [Online] Available at: < https://learn.microsoft.com/en-us/aspnet/core/fundamentals/dependency-injection > [Accessed 17 September 2026].
 
*/
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using CSharpBenchmark.infrastructure.socialmedia;

namespace CSharpBenchmark.presentation.post
{
    //Functional requirement 3.2.2
    //Test case 166: The classes in package presentation.post are not allowed to use modules in a not direct lower layer
    //Result: FALSE
    public class PostSpotifyMessage
    {
        public PostSpotifyMessage()
        {
            try
            {
                //FR5.8
            }
            catch (SocialMediaException e)
            {
                Console.WriteLine(e.StackTrace);
            }
        }
    }
}
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using CSharpBenchmark.infrastructure.socialmedia.pinterest;
using CSharpBenchmark.presentation.upload.pinterest;

namespace CSharpBenchmark.presentation.gui.pinterest
{
    //Functional requirement 4.3
    //Test case 194: Class presentation.gui.pinterest.PinterestController is not allowed to use the classes in the package presentation.upload.pinterest except when data is exchanged if necessary in Data Transfer Object 
    //-PinterestController calls PinDAO.countpinsOnGUI(PinterestGUI gui)
    //Result: FALSE

    //Test case 195: Class presentation.gui.pinterest.PinterestController is not allowed to use the classes in the package infrastructure.socialmedia.pinterest except when data is exchanged if necessary in Data Transfer Object 
    //-presentation.gui.pinterest.PinterestController calls method boardCalculation();
    //Result: FALSE

    //Functional requirement 4.3
    //Test case 196: Class presentation.gui.pinterest.PinterestController is not allowed to use the classes in the package infrastructure.socialmedia.pinterest except when data is exchanged if necessary in Data Transfer Object 
    //- presentation.gui.pinterest.PinterestController calls method login(PinterestUserDTO dto);
    //Result: TRUE

    //Functional requirement 4.3
    //Test case 197: Class presentation.gui.pinterest.PinterestController is not allowed to use the classes in the package infrastructure.socialmedia.pinterest except when data is exchanged if necessary in Data Transfer Object 
    //-presentation.gui.pinterest.PinterestController calls method getBoardDetails(this);
    //Result: TRUE


    public class PinterestController
    {
        private PinDAO dao;
        private PinterestGUI gui;
        private UploadPinterestPicture upload;

        public PinterestController()
        {
            dao = new PinDAO();
        }
        public void countPinsOnGUI()
        {
            upload.Post(gui);
        }

        public int boardCalculation()
        {
            return (int)Math.Round(Math.Round(dao.getBoardFromAPI().boardCalculation()));
        }

        public bool login(PinterestUserDTO dto)
        {
            return dao.login(dto);
        }

        public void getBoardDetails()
        {
            dao.getBoardDetails(this);
        }
    }
}